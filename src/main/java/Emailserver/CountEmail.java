package Emailserver;

public class CountEmail {
    int toRecruitment;
    int toSpam;
    int toSales;
    int toReception;

    public CountEmail(int toRecruitment, int toSpam, int toSales, int toReception) {
        this.toRecruitment = toRecruitment;
        this.toSpam = toSpam;
        this.toSales = toSales;
        this.toReception = toReception;
    }

    @Override
    public String toString() {
        return "CountEmail{" +
                "toRecruitment=" + toRecruitment +
                ", toSpam=" + toSpam +
                ", toSales=" + toSales +
                ", toReception=" + toReception +
                '}';
    }
}
