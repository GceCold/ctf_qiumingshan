package com.github.gdenga;

import java.math.BigDecimal;
import java.util.Stack;

/**
 * @author: gdenga
 * @date: 2019/8/1 23:08
 * @content:
 */

public class Calculator {
    private Stack<BigDecimal> numbers = new Stack<BigDecimal>();

    private Stack<Character> chs = new Stack<Character>();

    private boolean compare(char str) {
        if (chs.empty()) {

            return true;
        }
        char last = (char) chs.lastElement();
        switch (str) {
            case '*': {

                if (last == '+' || last == '-') {
                    return true;
                } else {
                    return false;
                }
            }
            case '/': {
                if (last == '+' || last == '-') {
                    return true;
                } else {
                    return false;
                }
            }

            case '+':
                return false;
            case '-':
                return false;
        }
        return true;
    }

    public BigDecimal caculate(String st) {
        StringBuffer sb = new StringBuffer(st);
        StringBuffer num = new StringBuffer();
        String tem = null;
        char next;
        while (sb.length() > 0) {
            tem = sb.substring(0, 1);
            sb.delete(0, 1);
            if (isNum(tem.trim())) {
                num.append(tem);
            } else {

                if (num.length() > 0 && !"".equals(num.toString().trim())) {
                    BigDecimal bd = new BigDecimal(num.toString().trim());
                    numbers.push(bd);
                    num.delete(0, num.length());
                }
                if (!chs.isEmpty()) {

                    while (!compare(tem.charAt(0))) {
                        caculate();
                    }
                }
                if (numbers.isEmpty()) {
                    num.append(tem);
                } else {
                    chs.push(new Character(tem.charAt(0)));
                }
                next = sb.charAt(0);
                if (next == '-') {
                    num.append(next);
                    sb.delete(0, 1);
                }

            }
        }
        BigDecimal bd = new BigDecimal(num.toString().trim());
        numbers.push(bd);
        while (!chs.isEmpty()) {
            caculate();
        }
        return numbers.pop();
    }


    BigDecimal result = null;

    private void caculate() {
        BigDecimal b = numbers.pop();
        BigDecimal a = null;
        a = numbers.pop();
        char ope = chs.pop();

        switch (ope) {
            case '+':
                result = a.add(b);
                numbers.push(result);
                break;
            case '-':
                result = a.subtract(b);
                numbers.push(result);
                break;
            case '*':
                result = a.multiply(b);
                numbers.push(result);
                break;
            case '/':
                result = a.divide(b);
                numbers.push(result);
                break;
        }
    }

    public String getResult() {
        return result.toString();
    }

    private boolean isNum(String num) {
        return num.matches("[0-9]");
    }


    public BigDecimal parse(String st) {
        int start = 0;
        StringBuffer sts = new StringBuffer(st);
        int end = -1;
        while ((end = sts.indexOf(")")) > 0) {
            String s = sts.substring(start, end + 1);
            int first = s.lastIndexOf("(");
            BigDecimal value = caculate(sts.substring(first + 1, end));
            sts.replace(first, end + 1, value.toString());
        }
        return caculate(sts.toString());
    }



}
