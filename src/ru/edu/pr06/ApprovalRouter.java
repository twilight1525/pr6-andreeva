package ru.edu.pr06;

public class ApprovalRouter {
    private static final long FINANCE_LIMIT = 500_040L;

    public static ApprovalRoute route(long sum, boolean urgent, RiskLevel risk, boolean hasPersonalData) {
        if (sum < 0) {
            throw new IllegalArgumentException("Ошибка: сумма не может быть отрицательной");
        }
        if (risk == null) {
            throw new IllegalArgumentException("Ошибка: уровень риска не задан");
        }
        if (hasPersonalData) {
            return ApprovalRoute.SECURITY_REVIEW;
        }
        if (risk == RiskLevel.HIGH) {
            return ApprovalRoute.DIRECTOR_REVIEW;
        }
        if (sum >= FINANCE_LIMIT) {
            return ApprovalRoute.FINANCE_REVIEW;
        }
        if (urgent) {
            return ApprovalRoute.FAST_TRACK;
        }
        return ApprovalRoute.STANDARD;
    }

    public static long getFinanceLimit() {
        return FINANCE_LIMIT;
    }
}