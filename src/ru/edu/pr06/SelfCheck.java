package ru.edu.pr06;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class SelfCheck {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== UNIT ===");
        runUnitChecks();
        System.out.println();
        System.out.println("=== DATA-DRIVEN ===");
        runDataDriven();
        System.out.println();
        System.out.println("=== INTERACTIVE ===");
        runInteractive();
        System.out.println();
        System.out.println("ИТОГО unit: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }

    private static void runUnitChecks() {
        long limit = ApprovalRouter.getFinanceLimit();

        checkRoute("STANDARD", 100_000L, false, RiskLevel.LOW, false, ApprovalRoute.STANDARD);
        checkRoute("FAST_TRACK", 100_000L, true, RiskLevel.LOW, false, ApprovalRoute.FAST_TRACK);
        checkRoute("FINANCE_REVIEW", limit, false, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW);
        checkRoute("DIRECTOR_REVIEW", 100_000L, false, RiskLevel.HIGH, false, ApprovalRoute.DIRECTOR_REVIEW);
        checkRoute("SECURITY_REVIEW", 100_000L, true, RiskLevel.LOW, true, ApprovalRoute.SECURITY_REVIEW);

        checkRoute("граница limit-1 -> FAST_TRACK", limit - 1, true, RiskLevel.LOW, false, ApprovalRoute.FAST_TRACK);
        checkRoute("граница limit -> FINANCE", limit, true, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW);
        checkRoute("граница limit+1 -> FINANCE", limit + 1, true, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW);

        checkRoute("риск > срочность", 100_000L, true, RiskLevel.HIGH, false, ApprovalRoute.DIRECTOR_REVIEW);
        checkRoute("персданные > всё", 600_000L, true, RiskLevel.HIGH, true, ApprovalRoute.SECURITY_REVIEW);

        checkThrows("сумма < 0", () -> ApprovalRouter.route(-1L, false, RiskLevel.LOW, false));
        checkThrows("risk null", () -> ApprovalRouter.route(100_000L, false, null, false));
    }

    private static void checkRoute(String name, long sum, boolean urgent, RiskLevel risk, boolean pd, ApprovalRoute expected) {
        try {
            ApprovalRoute actual = ApprovalRouter.route(sum, urgent, risk, pd);
            if (actual == expected) pass(name);
            else fail(name, "ожидалось " + expected + ", получено " + actual);
        } catch (Exception e) {
            fail(name, "исключение: " + e.getMessage());
        }
    }

    private static void checkThrows(String name, Runnable r) {
        try {
            r.run();
            fail(name, "ожидалось исключение");
        } catch (IllegalArgumentException e) { pass(name); }
        catch (Exception e) { fail(name, "тип: " + e.getClass().getSimpleName()); }
    }

    private static void pass(String n) { passed++; System.out.println("PASS: " + n); }
    private static void fail(String n, String r) { failed++; System.out.println("FAIL: " + n + " -> " + r); }

    private static void runDataDriven() {
        int total = 0, ok = 0;
        try {
            List<String> lines = Files.readAllLines(Paths.get("data/sample.csv"));
            for (String line : lines) {
                if (line == null || line.isBlank() || line.startsWith("#")) continue;
                total++;
                String[] p = line.split("\\|", -1);
                if (p.length < 2) { System.out.println("FAIL data/" + total + ": формат"); continue; }
                
                String[] in = p[0].split(";", -1);
                if (in.length != 4) { System.out.println("FAIL data/" + total + ": вход"); continue; }
                
                long sum = Long.parseLong(in[0]);
                boolean urgent = Boolean.parseBoolean(in[1]);
                RiskLevel risk = RiskLevel.valueOf(in[2]);
                boolean pd = Boolean.parseBoolean(in[3]);
                String exp = p[1].trim();
                String cmt = p.length > 2 ? p[2] : "";

                if (exp.startsWith("OK:")) {
                    try {
                        ApprovalRoute actual = ApprovalRouter.route(sum, urgent, risk, pd);
                        String expRoute = exp.substring(3).trim();
                        boolean match = actual.name().equals(expRoute);
                        System.out.println((match ? "PASS" : "FAIL") + " data: " + p[0] + " (" + cmt + ")");
                        if (match) ok++;
                    } catch (Exception e) {
                        System.out.println("FAIL data: " + p[0] + " -> " + e.getMessage());
                    }
                } else if (exp.startsWith("ERROR:")) {
                    try {
                        ApprovalRouter.route(sum, urgent, risk, pd);
                        System.out.println("FAIL data: " + p[0] + " -> ожидалась ошибка");
                    } catch (IllegalArgumentException e) {
                        String frag = exp.substring(6).trim();
                        boolean match = e.getMessage() != null && e.getMessage().contains(frag);
                        System.out.println((match ? "PASS" : "FAIL") + " data: " + p[0] + " (" + cmt + ")");
                        if (match) ok++;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения sample.csv: " + e.getMessage());
        }
        System.out.println("Data-driven: " + ok + "/" + total + " passed.");
    }

    private static void runInteractive() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Формат: sum;urgent;RISK;personalData (например 100000;true;LOW;false)");
        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) break;
            String line = sc.nextLine();
            if (line.equalsIgnoreCase("exit") || line.isEmpty()) break;
            try {
                String[] in = line.split(";", -1);
                if (in.length != 4) {
                    System.out.println("Ошибка: нужно 4 поля");
                    continue;
                }
                long sum = Long.parseLong(in[0].trim());
                boolean urgent = Boolean.parseBoolean(in[1].trim());
                RiskLevel risk = RiskLevel.valueOf(in[2].trim().toUpperCase());
                boolean pd = Boolean.parseBoolean(in[3].trim());
                System.out.println("Маршрут: " + ApprovalRouter.route(sum, urgent, risk, pd));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}