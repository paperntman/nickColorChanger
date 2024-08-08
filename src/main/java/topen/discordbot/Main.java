package topen.discordbot;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main extends ListenerAdapter {

    private static final Dotenv dotenv = Dotenv.load();
    public static final JDA jda = JDABuilder.createDefault(dotenv.get("TOKEN"))
            .addEventListeners(new ColorCommand()) // ColorCommand 리스너 추가
            .build();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break; // 'exit' 입력 시 종료
            }
            processCommand(input);
        }

        jda.shutdown(); // 봇 종료
    }

    public static void processCommand(String command) {
        String[] parts = command.split(" ");
        if (parts.length == 3 && parts[0].equalsIgnoreCase("delete")) {
            String guildId = parts[1];
            String roleName = parts[2];
            deleteRole(guildId, roleName);
        } else if(parts[0].equalsIgnoreCase("list")){
            String guildId = parts[1];
            listRole(guildId);
        }
        else {
           logger.info("잘못된 명령어 형식입니다. 'delete (guild_id) (role_name)' 형식으로 입력하세요.");
        }
    }

    private static void listRole(String guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            logger.info("해당 길드를 찾을 수 없습니다.");
            return;
        }

        List<Role> roleList = guild.getRoles();
        for (Role role : roleList) {
            String collected = guild.getMembersWithRoles(role).stream().map(Member::getNickname).collect(Collectors.joining());
            logger.info("{}({}) : {}", role.getName(), guild.getMembersWithRoles(role), collected);
        }


    }

    private static void deleteRole(String guildId, String roleName) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            logger.info("해당 길드를 찾을 수 없습니다.");
            return;
        }

        List<Role> roleList = guild.getRoles().stream().filter(role -> role.getName().startsWith(roleName)).toList();
        if (roleList.isEmpty()) {
            logger.info("해당 역할을 찾을 수 없습니다.");
            return;
        }

        roleList.forEach(role -> role.delete().queue(
                success -> logger.info("역할 '%s'이(가) 삭제되었습니다.".formatted(role.getName())),
                error -> logger.info("역할 삭제 중 오류가 발생했습니다: %s".formatted(error))
        ));
    }
}
