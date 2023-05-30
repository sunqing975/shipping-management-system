package com.shipping.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @className: com.shipping.entity.Policy
 * @author: Superman
 * @create: 2023-05-30 22:19
 * @description: TODO
 */
public class Policy {
    public int k;
    /* attribute string if leaf, otherwise null */
    public String attr;
    /**
     * 表示Cy
     */
    // Element c;			/* G_1 only for leaves */
    /**
     * 表示_Cy
     */
    // Element cp;		/* G_1 only for leaves */
    /* array of BswabePolicy and length is 0 for leaves */
    public Policy[] children;

    /* only used during encryption */
    // BswabePolynomial q;

    /* only used during decription */
    public boolean satisfiable;
    int min_leaves;
    public int attri;
    ArrayList<Integer> satl = new ArrayList<Integer>();
}
