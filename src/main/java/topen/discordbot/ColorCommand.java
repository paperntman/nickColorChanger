package topen.discordbot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class ColorCommand extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ColorCommand.class);


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        try {
            Guild guild = event.getGuild();
            if (event.getName().equals("color")) {
                handleColorCommand(event, guild);
            }
            // 다른 명령어 처리...
        } catch (Exception e) {
            logger.error("예기치 않은 오류: {}", e.getMessage());
        }
    }

    private void handleColorCommand(SlashCommandInteractionEvent event, Guild guild) {
        Member member = event.getMember();
        OptionMapping eventOption = event.getOption("name");
        assert eventOption != null;
        String color = ColorFetcher.fetchColorData(eventOption.getAsString());
        Color decoded = Color.decode(color);
        String roleName = "bot_" + color;

        logger.info("{} used command: {}", event.getUser().getName(), event.getCommandString());
        event.deferReply().queue();
        event.getHook().sendMessage(String.format("%s (%s) 색깔을 적용했습니다!", eventOption.getAsString(), color)).queue();

        // 기존 역할 제거
        assert member != null;
        removeExistingRoles(member, guild);

        // 새로운 역할 생성 및 색상 설정
        guild.createRole()
                .setName(roleName)
                .setColor(decoded)
                .queue(role -> {
                    // 역할이 성공적으로 생성된 후에 사용자에게 역할 추가
                    guild.addRoleToMember(member, role).queue(
                            success -> logger.info("Successfully added role %s to member %s".formatted(role.getName(), member.getNickname())),
                            failure -> logger.error("Failed to add role {} to member {}", role.getName(), member.getNickname())
                    );
                    movePositionUp(role);
                }, throwable -> {
                    logger.error("역할 생성 중 오류 발생: {}", throwable.getMessage());
                    event.getHook().sendMessage("역할 생성 중 오류가 발생했습니다.").queue();
                });

    }

    private void movePositionUp(Role role) {

        Role roleByBot = role.getGuild().getRoleByBot(Main.jda.getSelfUser());
        role.getGuild().modifyRolePositions().selectPosition(role).moveBelow(roleByBot).queue(
                success -> logger.info("positioning nicely done"),
                failure -> logger.error("error while positioning")
        );
    }

    private void removeExistingRoles(Member member, Guild guild) {
        member.getRoles().stream()
                .filter(role -> role.getName().startsWith("bot_"))
                .forEach(role -> guild.removeRoleFromMember(member, role).queue(
                        success -> logger.info("removed role %s from member %s".formatted(role.getName(), member.getNickname())),
                        failure -> logger.error("failed to remove role {} from member {}", role.getName(), member.getNickname())
                ));
    }

}
