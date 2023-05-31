package com.shipping;

/**
 * @className: com.shipping.constant.Role
 * @author: Superman
 * @create: 2023-05-17 21:41
 * @description: TODO
 */
public enum RoleConst {
    /**
     * 船员
     */
    MARINER(1),
    /**
     * 监管员
     */
    SUPERVISOR(2);

    private Integer role;

    RoleConst(int role) {
        this.role = role;
    }

    public Integer getRole() {
        return role;
    }
}
