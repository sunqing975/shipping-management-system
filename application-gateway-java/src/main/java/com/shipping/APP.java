package com.shipping;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shipping.client.MarinerClient;
import com.shipping.client.SupervisorClient;
import com.shipping.entity.*;
import com.shipping.service.ClientService;
import com.shipping.utils.JsonUtils;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.netty.util.internal.StringUtil;
import org.hyperledger.fabric.client.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;

public class APP {

    public static final String USER_CONTRACT = "UserContract";
    public static final String ATTRIBUTE_CONTRACT = "AttributeContract";
    public static final String EC_POLICY_CONTRACT = "ECPolicyContract";
    public static final String ENERGY_CONSUMPTION_CONTRACT = "EnergyConsumptionContract";
    public static final String USER_ATTRIBUTE_CONTRACT = "UserAttributeContract";

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
        while (!input.equals("0")) {
            switch (input) {
                case "1":
                    marinerLoginAndConn();
                    break;
                case "2":
                    supervisorLoginAndConn();
                    break;
                default:
                    System.out.println("输入异常，请重新输入");
                    input = scanner.next();
            }
        }
    }

    private static void supervisorLoginAndConn() {
        System.out.println("============欢迎监管员进入船运能耗管理系统============");
        System.out.println("==>0 退出系统");
        System.out.println("登录：");
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

        Contract userContract = client.getContract(channel, USER_CONTRACT);
        Contract attributeContract = client.getContract(channel, ATTRIBUTE_CONTRACT);
        Contract ecPolicyContract = client.getContract(channel, EC_POLICY_CONTRACT);
        Contract energyConsumptionContract = client.getContract(channel, ENERGY_CONSUMPTION_CONTRACT);
        Contract userAttributeContract = client.getContract(channel, USER_ATTRIBUTE_CONTRACT);

        ClientService service = new ClientService();

        Gson gson = new Gson();
        List<User> allUsers;
        try {
            // 初始化账本数据
            userContract.submitTransaction("initLedger");
            attributeContract.submitTransaction("initLedger");
            ecPolicyContract.submitTransaction("initLedger");
            energyConsumptionContract.submitTransaction("initLedger");
            userAttributeContract.submitTransaction("initLedger");

            byte[] result = userContract.evaluateTransaction("getAllUsers");
            String json = JsonUtils.prettyJson(result);
            Type type = new TypeToken<List<User>>() {
            }.getType();
            allUsers = gson.fromJson(json, type);
        } catch (GatewayException | CommitException e) {
            throw new RuntimeException(e);
        }
        if (Objects.isNull(allUsers)) {
            System.out.println("==账本无数据==");
            return;
        }
        while (true) {
            if (username.equals("0")) {
                break;
            }
            String userId = service.login(allUsers, username, 2);
            if (!StringUtil.isNullOrEmpty(userId)) {
                System.out.println("登录成功");
                try {
                    supervisorMenu(userContract, attributeContract, ecPolicyContract,
                            energyConsumptionContract, userAttributeContract, username, channel);
                } catch (GatewayException | CommitException e) {
                    throw new RuntimeException(e);
                }
                break;
            } else {
                System.out.println("登录失败，请重试。");
                System.out.println("请重新输入正确的用户名：");
                username = scanner.next();
            }
        }
    }


    private static void marinerLoginAndConn() {
        System.out.println("============欢迎船员进入船运能耗管理系统============");
        System.out.println("==>0 退出系统");
        System.out.println("登录：");
        System.out.println("===请输入您的用户名===");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.next();
        MarinerClient client = new MarinerClient();
        ManagedChannel channel;
        try {
            channel = client.newGrpcConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Contract userContract = client.getContract(channel, USER_CONTRACT);
        Contract attributeContract = client.getContract(channel, ATTRIBUTE_CONTRACT);
        Contract ecPolicyContract = client.getContract(channel, EC_POLICY_CONTRACT);
        Contract energyConsumptionContract = client.getContract(channel, ENERGY_CONSUMPTION_CONTRACT);
        Contract userAttributeContract = client.getContract(channel, USER_ATTRIBUTE_CONTRACT);

        ClientService service = new ClientService();

        Gson gson = new Gson();
        List<User> allUsers;
        try {
            // 初始化账本数据
            userContract.submitTransaction("initLedger");
            attributeContract.submitTransaction("initLedger");
            ecPolicyContract.submitTransaction("initLedger");
            energyConsumptionContract.submitTransaction("initLedger");
            userAttributeContract.submitTransaction("initLedger");

            byte[] result = userContract.evaluateTransaction("getAllUsers");
            String json = JsonUtils.prettyJson(result);
            Type type = new TypeToken<List<User>>() {
            }.getType();
            allUsers = gson.fromJson(json, type);
        } catch (GatewayException | CommitException e) {
            throw new RuntimeException(e);
        }
        if (Objects.isNull(allUsers)) {
            System.out.println("==账本无数据==");
            return;
        }
        while (true) {
            if (username.equals("0")) {
                client.closeChannel(channel);
                break;
            }
            String userId = service.login(allUsers, username, 1);
            if (!StringUtil.isNullOrEmpty(userId)) {
                System.out.println("登录成功");
                try {
                    marinerMenu(attributeContract, ecPolicyContract,
                            energyConsumptionContract, userAttributeContract, userId, username, channel);
                } catch (GatewayException e) {
                    throw new RuntimeException(e);
                }
                break;
            } else {
                System.out.println("登录失败，请重试。");
                System.out.println("请重新输入正确的用户名：");
                username = scanner.next();
            }
        }
    }

    private static void supervisorMenu(Contract userContract, Contract attributeContract, Contract ecPolicyContract,
                                       Contract energyConsumptionContract, Contract userAttributeContract,
                                       String username, ManagedChannel channel) throws GatewayException, CommitException {
        System.out.println("============欢迎" + username + "进入船运能耗管理系统============");
        System.out.println("===请选择功能（选择序号）");
        System.out.println("==>1 查询所有能耗信息");
        System.out.println("==>2 查询具体的能耗信息");
        System.out.println("==>3 更新具体的能耗信息");
        System.out.println("==>4 删除具体的能耗信息");

        System.out.println("==>5 查询所有的属性信息");
        System.out.println("==>6 新增属性信息");
        System.out.println("==>7 修改属性信息");

        System.out.println("==>8 查询所有用户信息");
        System.out.println("==>9 新增用户信息");
        System.out.println("==>10 修改用户信息");
        System.out.println("==>11 删除用户信息及属性关联");

        System.out.println("==>12 查询用户的属性信息");
        System.out.println("==>13 分配与修改用户的属性");

        System.out.println("==>0 退出系统");

        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        Gson gson = new Gson();
        while (!input.equals("0")) {
            switch (input) {
                case "1":
                    // 查询所有能耗信息
                    byte[] result1 = energyConsumptionContract.evaluateTransaction("getAllEnergyConsumptions");
                    String json1 = JsonUtils.prettyJson(result1);
                    System.out.println("所有的能耗信息：" + json1);
                    break;
                case "2":
                    // 查询具体的能耗信息
                    System.out.println("==请输入查询的能耗信息ID==");
                    String ecId = scanner.next();
                    byte[] result2 = energyConsumptionContract.evaluateTransaction("readEnergyConsumption", ecId);
                    String json2 = JsonUtils.prettyJson(result2);
                    System.out.println("能耗信息：" + json2);
                    break;
                case "3":
                    // 更新具体的能耗信息
                    System.out.println("==请输入需要更新的能耗信息(不输入则使用默认值)==");
                    System.out.println("==请输入需要更新的能耗信息ID==");
                    String ecId_update = scanner.next();
                    byte[] result3 = energyConsumptionContract.evaluateTransaction("readEnergyConsumption", ecId_update);
                    String json3 = JsonUtils.prettyJson(result3);
                    EnergyConsumption oldEnergyConsumption = gson.fromJson(json3, EnergyConsumption.class);
                    System.out.println("原本信息：" + json3);

                    System.out.println("==请输入能耗信息的机构代码==");
                    String orgCode_update = scanner.next();
                    System.out.println("==请输入能耗信息的船只编号==");
                    String shipId_update = scanner.next();
                    System.out.println("==请输入能耗信息的船只名称==");
                    String shipName_update = scanner.next();
                    System.out.println("==请输入能耗信息的船只类型==");
                    String shipType_update = scanner.next();
                    System.out.println("==请输入能耗信息的船尺寸==");
                    String shipSize_update = scanner.next();
                    System.out.println("==请输入能耗信息的能源类型==");
                    String energyType_update = scanner.next();
                    System.out.println("==请输入能耗信息的能源名称==");
                    String energyName_update = scanner.next();
                    System.out.println("==请输入能耗信息的能耗数量==");
                    String consumeQuantity_update = scanner.next();
                    System.out.println("==请输入能耗信息的能耗数量单位==");
                    String consumeQuantityUnit_update = scanner.next();
                    System.out.println("==请输入能耗信息的上传者ID==");
                    String operatorId_update = scanner.next();

                    energyConsumptionContract.submitTransaction("updateEnergyConsumption", ecId_update,
                            StringUtil.isNullOrEmpty(orgCode_update) ? String.valueOf(oldEnergyConsumption.getOrgCode()) : orgCode_update,
                            StringUtil.isNullOrEmpty(shipId_update) ? String.valueOf(oldEnergyConsumption.getShipId()) : shipId_update,
                            StringUtil.isNullOrEmpty(shipName_update) ? oldEnergyConsumption.getShipName() : shipName_update,
                            StringUtil.isNullOrEmpty(shipType_update) ? oldEnergyConsumption.getShipType() : shipType_update,
                            StringUtil.isNullOrEmpty(shipSize_update) ? String.valueOf(oldEnergyConsumption.getShipSize()) : shipSize_update,
                            StringUtil.isNullOrEmpty(energyType_update) ? String.valueOf(oldEnergyConsumption.getEnergyType()) : energyType_update,
                            StringUtil.isNullOrEmpty(energyName_update) ? oldEnergyConsumption.getEnergyName() : energyName_update,
                            StringUtil.isNullOrEmpty(consumeQuantity_update) ? String.valueOf(oldEnergyConsumption.getConsumeQuantity()) : consumeQuantity_update,
                            StringUtil.isNullOrEmpty(consumeQuantityUnit_update) ? oldEnergyConsumption.getConsumeQuantityUnit() : consumeQuantityUnit_update,
                            StringUtil.isNullOrEmpty(operatorId_update) ? oldEnergyConsumption.getOperatorId() : operatorId_update,
                            String.valueOf(oldEnergyConsumption.getStartTime()), String.valueOf(System.currentTimeMillis())
                    );
                    System.out.println("更新成功");
                    break;
                case "4":
                    // 删除具体的能耗信息
                    System.out.println("==请输入删除的能耗信息ID==");
                    String ecId_delete = scanner.next();
                    energyConsumptionContract.submitTransaction("deleteEnergyConsumption", ecId_delete);

                    byte[] result4 = ecPolicyContract.evaluateTransaction("getAllECPolicys");
                    String json4 = JsonUtils.prettyJson(result4);
                    Type type4 = new TypeToken<List<ECPolicy>>() {
                    }.getType();
                    List<ECPolicy> ecPolicyList4 = gson.fromJson(json4, type4);
                    for (ECPolicy ecPolicy : ecPolicyList4) {
                        if (ecPolicy.getEcId().equals(ecId_delete)) {
                            ecPolicyContract.submitTransaction("deleteECPolicy", ecPolicy.getId());
                        }
                    }
                    System.out.println("删除成功");
                    break;
                case "5":
                    // 查询所有的属性信息
                    byte[] result5 = attributeContract.evaluateTransaction("getAllAttributes");
                    String json5 = JsonUtils.prettyJson(result5);
                    System.out.println("所有的属性：" + json5);
                    break;
                case "6":
                    // 新增属性信息
                    String attributeID = "att" + Instant.now().toEpochMilli();
                    System.out.println("==请输入属性名称==");
                    String attName = scanner.next();
                    System.out.println("==请输入属性值==");
                    String attValue = scanner.next();
                    attributeContract.evaluateTransaction("createAttribute", attributeID, attName, attValue);
                    System.out.println("新增成功");
                    break;
                case "7":
                    // 修改属性信息
                    System.out.println("==请输入需要更新的属性ID==");
                    String att_id_update = scanner.next();
                    byte[] result7 = attributeContract.evaluateTransaction("readAttribute", att_id_update);
                    String json7 = JsonUtils.prettyJson(result7);
                    System.out.println("原属性信息：" + json7);
                    Attribute oldAttribute = gson.fromJson(json7, Attribute.class);
                    System.out.println("==请输入需要更新的属性名==");
                    String att_name_update = scanner.next();
                    System.out.println("==请输入需要更新的属性值==");
                    String att_value_update = scanner.next();

                    attributeContract.submitTransaction("updateAttribute",
                            att_id_update,
                            StringUtil.isNullOrEmpty(att_name_update) ? oldAttribute.getName() : att_name_update,
                            StringUtil.isNullOrEmpty(att_value_update) ? oldAttribute.getValue() : att_value_update
                    );
                    System.out.println("修改成功");

                    break;
                case "8":
                    // 查询所有用户信息
                    byte[] result8 = userContract.evaluateTransaction("getAllUsers");
                    String json8 = JsonUtils.prettyJson(result8);
                    System.out.println("所有的用户信息：" + json8);
                    break;
                case "9":
                    // 新增用户信息
                    String userID = "user" + Instant.now().toEpochMilli();
                    System.out.println("==请输入用户名称==");
                    String username_insert = scanner.next();
                    System.out.println("==请输入用户角色（1:船员；2:监管员）==");
                    String role_insert = scanner.next();
                    userContract.submitTransaction("createUser", userID, username_insert, role_insert);
                    System.out.println("新增成功");
                    break;
                case "10":
                    // 修改用户信息
                    System.out.println("==请输入需要更新的用户ID==");
                    String user_id_update = scanner.next();

                    byte[] result10 = userContract.evaluateTransaction("readUser", user_id_update);
                    String json10 = JsonUtils.prettyJson(result10);
                    System.out.println("原用户信息：" + json10);
                    User oldUser = gson.fromJson(json10, User.class);
                    System.out.println("==请输入需要更新的用户名==");
                    String username_update = scanner.next();

                    byte[] result10_1 = userContract.evaluateTransaction("getAllUsers");
                    String json10_1 = JsonUtils.prettyJson(result10_1);
                    Type type10 = new TypeToken<List<User>>() {
                    }.getType();
                    List<User> users = gson.fromJson(json10_1, type10);
                    boolean flag10 = true;
                    for (User user : users) {
                        if (user.getUsername().equals(username_update)) {
                            flag10 = false;
                            break;
                        }
                    }
                    if (!flag10) {
                        System.out.println("修改失败，存在相同用户名");
                    } else {
                        System.out.println("==请输入用户角色（1:船员；2:监管员）==");
                        String role_update = scanner.next();
                        userContract.submitTransaction("updateUser",
                                user_id_update,
                                StringUtil.isNullOrEmpty(username_update) ? oldUser.getUsername() : username_update,
                                StringUtil.isNullOrEmpty(role_update) ? String.valueOf(oldUser.getRole()) : role_update);
                        System.out.println("修改成功");
                    }
                    break;
                case "11":
                    // 删除用户信息及属性关联
                    System.out.println("==请输入需要删除的属性ID==");
                    String user_id_delete = scanner.next();
                    userContract.submitTransaction("deleteUser", user_id_delete);

                    byte[] result11 = userAttributeContract.evaluateTransaction("getAllUserAttributes");
                    String json11 = JsonUtils.prettyJson(result11);
                    Type type11 = new TypeToken<List<UserAttribute>>() {
                    }.getType();
                    List<UserAttribute> userAttributes = gson.fromJson(json11, type11);
                    for (UserAttribute userAttribute : userAttributes) {
                        if (userAttribute.getUserId().equals(user_id_delete)) {
                            userAttributeContract.submitTransaction("deleteUserAttribute", userAttribute.getId());
                        }
                    }
                    System.out.println("删除成功");
                    break;
                case "12":
                    // 查询用户的属性信息
                    System.out.println("==请输入用户ID==");
                    String user_id_select = scanner.next();
                    byte[] result12 = userContract.evaluateTransaction("readUser", user_id_select);
                    String json12 = JsonUtils.prettyJson(result12);
                    User user = gson.fromJson(json12, User.class);

                    byte[] result12_1 = userAttributeContract.evaluateTransaction("getAllUserAttributes");
                    String json12_1 = JsonUtils.prettyJson(result12_1);
                    Type type12 = new TypeToken<List<UserAttribute>>() {
                    }.getType();
                    List<Attribute> userAttrs = new ArrayList<>();
                    List<UserAttribute> userAttributes12 = gson.fromJson(json12_1, type12);
                    for (UserAttribute userAttribute : userAttributes12) {
                        if (userAttribute.getUserId().equals(user.getId())) {
                            byte[] result = userAttributeContract.evaluateTransaction("readUserAttribute",
                                    userAttribute.getId());
                            String json = JsonUtils.prettyJson(result);
                            UserAttribute userWithAttribute = gson.fromJson(json, UserAttribute.class);
                            byte[] result12_2 = attributeContract.evaluateTransaction("readAttribute",
                                    userWithAttribute.getAttributeId());
                            String json12_2 = JsonUtils.prettyJson(result12_2);
                            Attribute attribute = gson.fromJson(json12_2, Attribute.class);
                            userAttrs.add(attribute);
                        }
                    }
                    System.out.println("用户的基本信息为：" + user.toString());
                    System.out.println("用户的属性为：" + userAttrs);
                    break;
                case "13":
                    // 分配与修改用户的属性
                    System.out.println("==请输入用户ID==");
                    String user_id = scanner.next();

                    byte[] result = userAttributeContract.evaluateTransaction("getAllUserAttributes");
                    String json13 = JsonUtils.prettyJson(result);

                    Type type13 = new TypeToken<List<UserAttribute>>() {
                    }.getType();
                    List<UserAttribute> userAttributes13 = gson.fromJson(json13, type13);
                    for (UserAttribute userAttribute : userAttributes13) {
                        if (userAttribute.getUserId().equals(user_id)) {
                            userAttributeContract.submitTransaction("deleteUserAttribute", userAttribute.getId());
                        }
                    }
                    System.out.println("==请输入赋予用户属性的ID集合（用逗号间隔）==");
                    String att_ids = scanner.next();
                    if (StringUtil.isNullOrEmpty(att_ids)) {
                        System.out.println("输入为空，异常！");
                        System.out.println("请重试！");
                    } else {
                        String[] attIds = att_ids.split(",");
                        // 每一个用户的属性是唯一的，即同一个属性只能有一个值
                        Set<String> set = new HashSet<>();
                        for (String attId : attIds) {
                            byte[] result_att = attributeContract.evaluateTransaction("readAttribute", attId);
                            String json_att = JsonUtils.prettyJson(result_att);
                            Attribute attribute = gson.fromJson(json_att, Attribute.class);
                            if (set.contains(attribute.getName())) {
                                System.out.println("属性数据输入异常，存在重复属性！");
                                System.out.println("请重试！");
                                break;
                            } else {
                                set.add(attribute.getName());
                            }
                        }
                        // 数据正常
                        for (String attId : attIds) {
                            String id = "ua" + Instant.now().toEpochMilli();
                            userAttributeContract.submitTransaction("createUserAttribute", id, user_id, attId);
                        }
                        System.out.println("分配或修改成功");
                    }

                    break;
                default:
                    System.out.println("输入异常，请重新输入");
                    input = scanner.next();
            }
        }
        new SupervisorClient().closeChannel(channel);
    }

    private static void marinerMenu(Contract attributeContract, Contract ecPolicyContract,
                                    Contract energyConsumptionContract, Contract userAttributeContract, String userId,
                                    String username, ManagedChannel channel)
            throws GatewayException {
        System.out.println("============欢迎" + username + "进入船运能耗管理系统============");
        System.out.println("===请选择功能（选择序号）");
        System.out.println("==>1 查询具体的能耗信息");
        System.out.println("==>2 添加具体的能耗信息及访问策略");
        System.out.println("==>3 更新具体的能耗信息及访问策略");

        System.out.println("==>4 查询所有的属性信息");

        System.out.println("==>0 退出系统");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        Gson gson = new Gson();
        ClientService service = new ClientService();

        // 查询当前船员的属性集
        List<Attribute> userAttrs = new ArrayList<>();
        byte[] result = userAttributeContract.evaluateTransaction("getAllUserAttributes");
        String json = JsonUtils.prettyJson(result);
        Type type = new TypeToken<List<UserAttribute>>() {
        }.getType();
        List<UserAttribute> userAttributes = gson.fromJson(json, type);
        for (UserAttribute userAttribute : userAttributes) {
            if (userAttribute.getUserId().equals(userId)) {
                byte[] result_1 = userAttributeContract.evaluateTransaction("readUserAttribute", userAttribute.getId());
                String json_1 = JsonUtils.prettyJson(result_1);
                UserAttribute userWithAttribute = gson.fromJson(json_1, UserAttribute.class);
                byte[] result_2 = attributeContract.evaluateTransaction("readAttribute", userWithAttribute.getAttributeId());
                String json_2 = JsonUtils.prettyJson(result_2);
                Attribute attribute = gson.fromJson(json_2, Attribute.class);
                userAttrs.add(attribute);
            }
        }

        while (!"0".equals(input)) {
            switch (input) {
                case "1":
                    // 查询具体的能耗信息
                    System.out.println("==请输入查询的能耗信息ID==");
                    String ecId = scanner.next();

                    byte[] result1 = ecPolicyContract.evaluateTransaction("getAllECPolicys");
                    String allPolicyJsons = JsonUtils.prettyJson(result1);
                    Type type1 = new TypeToken<List<ECPolicy>>() {
                    }.getType();
                    List<ECPolicy> ecPolicyList = gson.fromJson(allPolicyJsons, type1);
                    String policy = null;
                    for (ECPolicy ecPolicy : ecPolicyList) {
                        if (ecPolicy.getEcId().equals(ecId)) {
                            policy = ecPolicy.getPolicy();
                            break;
                        }
                    }
                    boolean flag = service.attributeCheck(userAttrs, policy);
                    if (!flag) {
                        System.err.println("attributes do not satisfy policy");
                    } else {
                        byte[] result2 = energyConsumptionContract.evaluateTransaction("readEnergyConsumption", ecId);
                        String ecJson = JsonUtils.prettyJson(result2);
                        System.out.println(ecJson);
                    }
                    break;
                case "2":
                    // 添加具体的能耗信息及访问策略
                    String ecID = "ec" + Instant.now().toEpochMilli();

                    System.out.println("==请输入能耗信息的机构代码==");
                    String orgCode_insert = scanner.next();
                    System.out.println("==请输入能耗信息的船只编号==");
                    String shipId_insert = scanner.next();
                    System.out.println("==请输入能耗信息的船只名称==");
                    String shipName_insert = scanner.next();
                    System.out.println("==请输入能耗信息的船只类型==");
                    String shipType_insert = scanner.next();
                    System.out.println("==请输入能耗信息的船尺寸==");
                    String shipSize_insert = scanner.next();
                    System.out.println("==请输入能耗信息的能源类型==");
                    String energyType_insert = scanner.next();
                    System.out.println("==请输入能耗信息的能源名称==");
                    String energyName_insert = scanner.next();
                    System.out.println("==请输入能耗信息的能耗数量==");
                    String consumeQuantity_insert = scanner.next();
                    System.out.println("==请输入能耗信息的能耗数量单位==");
                    String consumeQuantityUnit_insert = scanner.next();

                    String ecpolicyId = "ecpolicy" + Instant.now().toEpochMilli();
                    System.out.println("==请输入能耗信息的访问策略==");
                    String policy_insert = scanner.next();

                    String curTime = String.valueOf(System.currentTimeMillis());
                    try {
                        energyConsumptionContract.submitTransaction("createEnergyConsumption", ecID,
                                orgCode_insert,
                                shipId_insert,
                                shipName_insert,
                                shipType_insert,
                                shipSize_insert,
                                energyType_insert,
                                energyName_insert,
                                consumeQuantity_insert,
                                consumeQuantityUnit_insert,
                                userId,
                                curTime,
                                curTime
                        );
                        ecPolicyContract.submitTransaction("createECPolicy", ecpolicyId, ecID, policy_insert);
                    } catch (CommitException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("添加成功");
                    break;
                case "3":
                    // 更新具体的能耗信息及访问策略
                    System.out.println("==请输入需要更新的能耗信息(不输入则使用默认值)==");

                    System.out.println("==请输入需要更新的能耗信息ID==");
                    String ecId_update = scanner.next();

                    byte[] result31 = ecPolicyContract.evaluateTransaction("getAllECPolicys");
                    String allPolicyJsons3 = JsonUtils.prettyJson(result31);
                    Type type31 = new TypeToken<List<ECPolicy>>() {
                    }.getType();
                    List<ECPolicy> ecPolicys = gson.fromJson(allPolicyJsons3, type31);

                    ECPolicy oldECPolicy = null;
                    for (ECPolicy ecPolicy : ecPolicys) {
                        if (ecPolicy.getEcId().equals(ecId_update)) {
                            oldECPolicy = ecPolicy;
                            break;
                        }
                    }
                    assert oldECPolicy != null;
                    boolean flag3 = service.attributeCheck(userAttrs, oldECPolicy.getPolicy());
                    if (!flag3) {
                        System.err.println("attributes do not satisfy policy");
                    } else {
                        byte[] result3 = energyConsumptionContract.evaluateTransaction("readEnergyConsumption", ecId_update);
                        String json3 = JsonUtils.prettyJson(result3);
                        EnergyConsumption oldEnergyConsumption = gson.fromJson(json3, EnergyConsumption.class);
                        System.out.println("原本信息：" + json3);

                        System.out.println("==请输入能耗信息的机构代码==");
                        String orgCode_update = scanner.next();
                        System.out.println("==请输入能耗信息的船只编号==");
                        String shipId_update = scanner.next();
                        System.out.println("==请输入能耗信息的船只名称==");
                        String shipName_update = scanner.next();
                        System.out.println("==请输入能耗信息的船只类型==");
                        String shipType_update = scanner.next();
                        System.out.println("==请输入能耗信息的船尺寸==");
                        String shipSize_update = scanner.next();
                        System.out.println("==请输入能耗信息的能源类型==");
                        String energyType_update = scanner.next();
                        System.out.println("==请输入能耗信息的能源名称==");
                        String energyName_update = scanner.next();
                        System.out.println("==请输入能耗信息的能耗数量==");
                        String consumeQuantity_update = scanner.next();
                        System.out.println("==请输入能耗信息的能耗数量单位==");
                        String consumeQuantityUnit_update = scanner.next();

                        System.out.println("==请输入能耗信息的访问策略==");
                        String policy_update = scanner.next();

                        try {
                            energyConsumptionContract.submitTransaction("updateEnergyConsumption", ecId_update,
                                    StringUtil.isNullOrEmpty(orgCode_update) ? String.valueOf(oldEnergyConsumption.getOrgCode()) : orgCode_update,
                                    StringUtil.isNullOrEmpty(shipId_update) ? String.valueOf(oldEnergyConsumption.getShipId()) : shipId_update,
                                    StringUtil.isNullOrEmpty(shipName_update) ? oldEnergyConsumption.getShipName() : shipName_update,
                                    StringUtil.isNullOrEmpty(shipType_update) ? oldEnergyConsumption.getShipType() : shipType_update,
                                    StringUtil.isNullOrEmpty(shipSize_update) ? String.valueOf(oldEnergyConsumption.getShipSize()) : shipSize_update,
                                    StringUtil.isNullOrEmpty(energyType_update) ? String.valueOf(oldEnergyConsumption.getEnergyType()) : energyType_update,
                                    StringUtil.isNullOrEmpty(energyName_update) ? oldEnergyConsumption.getEnergyName() : energyName_update,
                                    StringUtil.isNullOrEmpty(consumeQuantity_update) ? String.valueOf(oldEnergyConsumption.getConsumeQuantity()) : consumeQuantity_update,
                                    StringUtil.isNullOrEmpty(consumeQuantityUnit_update) ? oldEnergyConsumption.getConsumeQuantityUnit() : consumeQuantityUnit_update,
                                    oldEnergyConsumption.getOperatorId(),
                                    String.valueOf(oldEnergyConsumption.getStartTime()), String.valueOf(System.currentTimeMillis())
                            );

                            ecPolicyContract.submitTransaction("updateECPolicy",
                                    oldECPolicy.getId(),
                                    ecId_update,
                                    StringUtil.isNullOrEmpty(policy_update) ? oldECPolicy.getPolicy() : policy_update);
                        } catch (CommitException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("更新成功");
                    }
                    break;
                case "4":
                    // 查询所有的属性信息
                    byte[] result4 = attributeContract.evaluateTransaction("getAllAttributes");
                    String json4 = JsonUtils.prettyJson(result4);
                    System.out.println("所有的属性：" + json4);
                    break;
                default:
                    System.out.println("输入异常，请重新输入");
                    input = scanner.next();
            }
        }
        new MarinerClient().closeChannel(channel);
    }
}
