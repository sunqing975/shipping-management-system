import com.shipping.client.SupervisorClient;
import com.shipping.entity.User;
import com.shipping.service.ClientService;
import io.grpc.ManagedChannel;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.GatewayException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class APP {
    public static void main(String[] args) {
        showMenu();
    }

    private static void showMenu() {
        System.out.println("============欢迎进入船运能耗管理系统============");
        System.out.println("===请选择您的身份（选择序号）");
        System.out.println("==>1 船员");
        System.out.println("==>2 监管员");
        System.out.println("==>0 退出系统");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        LOOP:
        while (true) {
            switch (input) {
                case "1":
                    showMarinerMenu();
                    break LOOP;
                case "2":
                    showSupervisorMenu();
                    break LOOP;
                case "0":
                    break LOOP;
                default:
                    System.out.println("输入异常，请重新输入");
                    input = scanner.next();
            }
        }
    }

    private static void showSupervisorMenu() {
        System.out.println("===请输入您的用户名===");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.next();
        SupervisorClient client = new SupervisorClient();
        ManagedChannel channel;
        try {
            channel = client.newGrpcConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Contract contract = client.getContract(channel);
        ClientService service = new ClientService();

        List<User> allUsers;
        try {
            allUsers = client.getAllUsers(contract);
        } catch (GatewayException e) {
            throw new RuntimeException(e);
        }
        if (Objects.isNull(allUsers)) {
            System.out.println("==账本无数据==");
            return;
        }
        while (true) {
            boolean login = service.login(allUsers, username);
            if (login) {

                break;
            } else {
                System.out.println("登录失败，请重试。");
                System.out.println("请重新输入正确的用户名：");
                username = scanner.next();
            }
        }


    }

    private static void showMarinerMenu() {
    }

}
