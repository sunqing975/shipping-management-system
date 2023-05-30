package com.shipping.service;

import com.shipping.entity.Attribute;
import com.shipping.entity.Policy;
import com.shipping.entity.User;

import java.util.*;

public class ClientService {

    public String login(List<User> users, String username, int role) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getRole() == role) {
                return user.getId();
            }
        }
        return null;
    }

    public boolean attributeCheck(List<Attribute> userAttrs, String policyStr) {

        Policy policy = parsePolicyPostfix(policyStr);
        checkSatisfy(policy, userAttrs);
        return policy.satisfiable;
    }

    public static String[] parseAttribute(List<Attribute> userAttrs) {

        StringBuilder builder = new StringBuilder();
        for (Attribute attribute : userAttrs) {
            builder.append(attribute.getName()).append(":").append(attribute.getValue()).append(" ");
        }
        String s = builder.toString();

        ArrayList<String> str_arr = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(s);
        String token;
        String res[];
        int len;

        while (st.hasMoreTokens()) {
            token = st.nextToken();
            if (token.contains(":")) {
                str_arr.add(token);
            } else {
                System.out.println("Some error happens in the input attribute");
                System.exit(0);
            }
        }

        Collections.sort(str_arr, new SortByAlphabetic());

        len = str_arr.size();
        res = new String[len];
        for (int i = 0; i < len; i++) {
            res[i] = str_arr.get(i);
        }
        return res;
    }

    static class SortByAlphabetic implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            if (s1.compareTo(s2) >= 0) {
                return 1;
            }
            return 0;
        }

    }

    public static Policy parsePolicyPostfix(String s) {
        String[] toks;
        String tok;
        ArrayList<Policy> stack = new ArrayList<>();
        Policy root;

        toks = s.split(" ");

        int toks_cnt = toks.length;
        for (int index = 0; index < toks_cnt; index++) {
            int i, k, n;

            tok = toks[index];
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
                prvAttr = userAttrs.get(i).getId();
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
