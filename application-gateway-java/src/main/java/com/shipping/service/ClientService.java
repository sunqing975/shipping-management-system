package com.shipping.service;

import com.shipping.entity.Attribute;
import com.shipping.entity.Policy;
import com.shipping.entity.User;

import java.util.*;

public class ClientService {

    public String login(List<User> users, String username, int role) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getRole() == role) {
                return user.getID();
            }
        }
        return null;
    }

    public boolean attributeCheck(List<Attribute> userAttrs, String policyStr) {
        Policy policy = parsePolicyPostfix(policyStr);
        assert policy != null;
        checkSatisfy(policy, userAttrs);
        return policy.satisfiable;
    }

    private static Policy parsePolicyPostfix(String s) {
        String[] toks;
        String tok;
        ArrayList<Policy> stack = new ArrayList<>();
        Policy root;

        toks = s.split(" ");

        for (String value : toks) {
            int i, k, n;

            tok = value;
            if (!tok.contains("of")) {
                stack.add(baseNode(1, tok));
            } else {
                Policy node;

                /* parse kof n node */
                String[] k_n = tok.split("of");
                k = Integer.parseInt(k_n[0]);
                n = Integer.parseInt(k_n[1]);

                if (k < 1) {
                    System.out.println("error parsing " + s
                            + ": trivially satisfied operator " + tok);
                    return null;
                } else if (k > n) {
                    System.out.println("error parsing " + s
                            + ": unsatisfiable operator " + tok);
                    return null;
                } else if (n == 1) {
                    System.out.println("error parsing " + s
                            + ": indentity operator " + tok);
                    return null;
                } else if (n > stack.size()) {
                    System.out.println("error parsing " + s
                            + ": stack underflow at " + tok);
                    return null;
                }

                /* pop n things and fill in children */
                node = baseNode(k, null);
                node.children = new Policy[n];

                for (i = n - 1; i >= 0; i--) {
                    node.children[i] = stack.remove(stack.size() - 1);
                }

                /* push result */
                stack.add(node);
            }
        }

        if (stack.size() > 1) {
            System.out.println("error parsing " + s
                    + ": extra node left on the stack");
            return null;
        } else if (stack.size() < 1) {
            System.out.println("error parsing " + s + ": empty policy");
            return null;
        }

        root = stack.get(0);
        return root;
    }

    private static Policy baseNode(int k, String s) {
        Policy p = new Policy();

        p.k = k;
        if (!(s == null)) {
            p.attr = s;
        } else {
            p.attr = null;
        }
        return p;
    }

    private static void checkSatisfy(Policy p, List<Attribute> userAttrs) {
        int i, l;
        String prvAttr;

        p.satisfiable = false;
        if (p.children == null || p.children.length == 0) {
            for (i = 0; i < userAttrs.size(); i++) {
                prvAttr = userAttrs.get(i).getID();
                if (prvAttr.compareTo(p.attr) == 0) {
                    // System.out.println("=staisfy=");
                    p.satisfiable = true;
                    p.attri = i;
                    break;
                }
            }
        } else {
            for (i = 0; i < p.children.length; i++) {
                checkSatisfy(p.children[i], userAttrs);
            }

            l = 0;
            for (i = 0; i < p.children.length; i++) {
                if (p.children[i].satisfiable) {
                    l++;
                }
            }

            if (l >= p.k) {
                p.satisfiable = true;
            }
        }
    }
}
