import java.io.BufferedReader;
import java.io.InputStreamReader;
import nl.tudelft.instrumentation.patching.*;
import nl.tudelft.instrumentation.runner.CallableTraceRunner;

public class Problem11 implements CallableTraceRunner<Void> {

    static BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

    private String[] inputs = { "D", "C", "J", "B", "I", "F", "A", "H", "G", "E" };

    public String a780728121 = "h";

    public String a1360225377 = "g";

    public String a729595174 = "f";

    public String a270033534 = "i";

    public int a553986020 = 6;

    public int a419289115 = 10;

    public String a61085062 = "h";

    public String a76449788 = "h";

    public int a1209890889 = 9;

    public int a547511656 = 11;

    public String a1304806974 = "i";

    public String a2007907442 = "f";

    public int a2128642633 = 12;

    public String a1643269988 = "i";

    public int a168638684 = 15;

    public String a894128990 = "g";

    public String a572079232 = "i";

    public int a1537379265 = 14;

    public String a1980207818 = "f";

    public int a1735071167 = 14;

    public int a2021551447 = 8;

    public int a553209804 = 5;

    public int a1392217129 = 12;

    public int a823314445 = 9;

    public String a512217640 = "e";

    public String a278675587 = "g";

    public int a71487061 = 15;

    public String a1353794423 = "f";

    public boolean cf = true;

    public int a1370900715 = 10;

    public int a521273111 = 4;

    public int a1023470345 = 9;

    public int a919251806 = 5;

    public int a399501280 = 8;

    public String a1283870923 = "i";

    public int a1824730115 = 13;

    public int a1575755525 = 10;

    public String a990334020 = "e";

    public String a1367961664 = "f";

    public int a1565409750 = 10;

    private void errorCheck() {
        if ((((OperatorTracker.myOperator("==", a1392217129, 10, 0)) && (a1304806974.equals("i"))) && (OperatorTracker.myOperator("==", a1537379265, 15, 1)))) {
            cf = false;
            Errors.__VERIFIER_error(0);
        }
        if ((((a270033534.equals("h")) && (a894128990.equals("g"))) && (OperatorTracker.myOperator("==", a1537379265, 12, 2)))) {
            cf = false;
            Errors.__VERIFIER_error(1);
        }
        if ((((OperatorTracker.myOperator("==", a2021551447, 6, 3)) && (OperatorTracker.myOperator("==", a71487061, 15, 4))) && (OperatorTracker.myOperator("==", a1537379265, 10, 5)))) {
            cf = false;
            Errors.__VERIFIER_error(2);
        }
        if ((((OperatorTracker.myOperator("==", a553209804, 8, 6)) && (OperatorTracker.myOperator("==", a919251806, 8, 7))) && (OperatorTracker.myOperator("!=", a1537379265, 9, 8)))) {
            cf = false;
            Errors.__VERIFIER_error(3);
        }
        if ((((OperatorTracker.myOperator("==", a547511656, 7, 9)) && (OperatorTracker.myOperator("==", a168638684, 8, 10))) && (OperatorTracker.myOperator("==", a1537379265, 14, 11)))) {
            cf = false;
            Errors.__VERIFIER_error(4);
        }
        if ((((OperatorTracker.myOperator("==", a1209890889, 10, 12)) && (OperatorTracker.myOperator("==", a1735071167, 13, 13))) && (OperatorTracker.myOperator("==", a1537379265, 11, 14)))) {
            cf = false;
            Errors.__VERIFIER_error(5);
        }
        if ((((OperatorTracker.myOperator("==", a1209890889, 11, 15)) && (OperatorTracker.myOperator("==", a1735071167, 13, 16))) && (OperatorTracker.myOperator("==", a1537379265, 11, 17)))) {
            cf = false;
            Errors.__VERIFIER_error(6);
        }
        if ((((a894128990.equals("e")) && (a780728121.equals("h"))) && (OperatorTracker.myOperator("==", a1537379265, 13, 18)))) {
            cf = false;
            Errors.__VERIFIER_error(7);
        }
        if ((((a76449788.equals("f")) && (OperatorTracker.myOperator("==", a71487061, 12, 19))) && (OperatorTracker.myOperator("==", a1537379265, 10, 20)))) {
            cf = false;
            Errors.__VERIFIER_error(8);
        }
        if ((((a76449788.equals("e")) && (OperatorTracker.myOperator("==", a71487061, 12, 21))) && (OperatorTracker.myOperator("==", a1537379265, 10, 22)))) {
            cf = false;
            Errors.__VERIFIER_error(9);
        }
        if ((((a1367961664.equals("g")) && (OperatorTracker.myOperator("==", a168638684, 13, 23))) && (OperatorTracker.myOperator("==", a1537379265, 14, 24)))) {
            cf = false;
            Errors.__VERIFIER_error(10);
        }
        if ((((OperatorTracker.myOperator("==", a1023470345, 16, 25)) && (OperatorTracker.myOperator("==", a1735071167, 10, 26))) && (OperatorTracker.myOperator("==", a1537379265, 11, 27)))) {
            cf = false;
            Errors.__VERIFIER_error(11);
        }
        if ((((OperatorTracker.myOperator("==", a553209804, 7, 28)) && (OperatorTracker.myOperator("==", a919251806, 8, 29))) && (OperatorTracker.myOperator("==", a1537379265, 9, 30)))) {
            cf = false;
            Errors.__VERIFIER_error(12);
        }
        if ((((OperatorTracker.myOperator("==", a2021551447, 5, 31)) && (OperatorTracker.myOperator("==", a71487061, 15, 32))) && (OperatorTracker.myOperator("==", a1537379265, 10, 33)))) {
            cf = false;
            Errors.__VERIFIER_error(13);
        }
        if ((((OperatorTracker.myOperator("==", a823314445, 16, 34)) && (OperatorTracker.myOperator("==", a1735071167, 9, 35))) && (OperatorTracker.myOperator("==", a1537379265, 11, 36)))) {
            cf = false;
            Errors.__VERIFIER_error(14);
        }
        if ((((OperatorTracker.myOperator("==", a521273111, 9, 37)) && (OperatorTracker.myOperator("==", a1735071167, 11, 38))) && (OperatorTracker.myOperator("==", a1537379265, 11, 39)))) {
            cf = false;
            Errors.__VERIFIER_error(15);
        }
        if ((((OperatorTracker.myOperator("==", a1575755525, 7, 40)) && (a894128990.equals("h"))) && (OperatorTracker.myOperator("==", a1537379265, 12, 41)))) {
            cf = false;
            Errors.__VERIFIER_error(16);
        }
        if ((((OperatorTracker.myOperator("==", a2021551447, 3, 42)) && (OperatorTracker.myOperator("==", a71487061, 15, 43))) && (OperatorTracker.myOperator("==", a1537379265, 10, 44)))) {
            cf = false;
            Errors.__VERIFIER_error(17);
        }
        if ((((a1304806974.equals("f")) && (a894128990.equals("e"))) && (OperatorTracker.myOperator("==", a1537379265, 12, 45)))) {
            cf = false;
            Errors.__VERIFIER_error(18);
        }
        if ((((a1980207818.equals("e")) && (OperatorTracker.myOperator("==", a71487061, 11, 46))) && (OperatorTracker.myOperator("==", a1537379265, 10, 47)))) {
            cf = false;
            Errors.__VERIFIER_error(19);
        }
        if ((((a1643269988.equals("f")) && (OperatorTracker.myOperator("==", a71487061, 13, 48))) && (OperatorTracker.myOperator("==", a1537379265, 10, 49)))) {
            cf = false;
            Errors.__VERIFIER_error(20);
        }
        if ((((OperatorTracker.myOperator("==", a1023470345, 9, 50)) && (OperatorTracker.myOperator("==", a1735071167, 10, 51))) && (OperatorTracker.myOperator("==", a1537379265, 11, 52)))) {
            cf = false;
            Errors.__VERIFIER_error(21);
        }
        if ((((a61085062.equals("i")) && (a1360225377.equals("g"))) && (OperatorTracker.myOperator("==", a1537379265, 8, 53)))) {
            cf = false;
            Errors.__VERIFIER_error(22);
        }
        if ((((OperatorTracker.myOperator("==", a2128642633, 9, 54)) && (OperatorTracker.myOperator("==", a168638684, 11, 55))) && (OperatorTracker.myOperator("==", a1537379265, 14, 56)))) {
            cf = false;
            Errors.__VERIFIER_error(23);
        }
        if ((((a894128990.equals("f")) && (a780728121.equals("h"))) && (OperatorTracker.myOperator("!=", a1537379265, 13, 57)))) {
            cf = false;
            Errors.__VERIFIER_error(24);
        }
        if ((((OperatorTracker.myOperator("==", a1575755525, 10, 58)) && (a894128990.equals("h"))) && (OperatorTracker.myOperator("==", a1537379265, 12, 59)))) {
            cf = false;
            Errors.__VERIFIER_error(25);
        }
        if ((((OperatorTracker.myOperator("==", a521273111, 2, 60)) && (OperatorTracker.myOperator("==", a1735071167, 11, 61))) && (OperatorTracker.myOperator("==", a1537379265, 11, 62)))) {
            cf = false;
            Errors.__VERIFIER_error(26);
        }
        if ((((a1643269988.equals("g")) && (OperatorTracker.myOperator("==", a71487061, 14, 63))) && (OperatorTracker.myOperator("==", a1537379265, 10, 64)))) {
            cf = false;
            Errors.__VERIFIER_error(27);
        }
        if ((((OperatorTracker.myOperator("==", a1209890889, 9, 65)) && (OperatorTracker.myOperator("==", a1735071167, 13, 66))) && (OperatorTracker.myOperator("==", a1537379265, 11, 67)))) {
            cf = false;
            Errors.__VERIFIER_error(28);
        }
        if ((((OperatorTracker.myOperator("==", a1370900715, 15, 68)) && (a780728121.equals("e"))) && (OperatorTracker.myOperator("==", a1537379265, 13, 69)))) {
            cf = false;
            Errors.__VERIFIER_error(29);
        }
        if ((((OperatorTracker.myOperator("==", a1824730115, 15, 70)) && (OperatorTracker.myOperator("==", a168638684, 15, 71))) && (OperatorTracker.myOperator("==", a1537379265, 14, 72)))) {
            cf = false;
            Errors.__VERIFIER_error(30);
        }
        if ((((OperatorTracker.myOperator("==", a1392217129, 12, 73)) && (a1360225377.equals("e"))) && (OperatorTracker.myOperator("==", a1537379265, 8, 74)))) {
            cf = false;
            Errors.__VERIFIER_error(31);
        }
        if ((((OperatorTracker.myOperator("==", a2128642633, 11, 75)) && (OperatorTracker.myOperator("==", a168638684, 11, 76))) && (OperatorTracker.myOperator("!=", a1537379265, 14, 77)))) {
            cf = false;
            Errors.__VERIFIER_error(32);
        }
        if ((((OperatorTracker.myOperator("==", a1392217129, 14, 78)) && (a1304806974.equals("i"))) && (OperatorTracker.myOperator("==", a1537379265, 15, 79)))) {
            cf = false;
            Errors.__VERIFIER_error(33);
        }
        if ((((a1980207818.equals("f")) && (OperatorTracker.myOperator("==", a71487061, 11, 80))) && (OperatorTracker.myOperator("==", a1537379265, 10, 81)))) {
            cf = false;
            Errors.__VERIFIER_error(34);
        }
        if ((((a1643269988.equals("h")) && (OperatorTracker.myOperator("==", a71487061, 14, 82))) && (OperatorTracker.myOperator("==", a1537379265, 10, 83)))) {
            cf = false;
            Errors.__VERIFIER_error(35);
        }
        if ((((OperatorTracker.myOperator("==", a419289115, 14, 84)) && (OperatorTracker.myOperator("==", a168638684, 10, 85))) && (OperatorTracker.myOperator("==", a1537379265, 14, 86)))) {
            cf = false;
            Errors.__VERIFIER_error(36);
        }
        if ((((a1980207818.equals("g")) && (OperatorTracker.myOperator("==", a71487061, 11, 87))) && (OperatorTracker.myOperator("==", a1537379265, 10, 88)))) {
            cf = false;
            Errors.__VERIFIER_error(37);
        }
        if ((((OperatorTracker.myOperator("==", a547511656, 11, 89)) && (OperatorTracker.myOperator("==", a168638684, 8, 90))) && (OperatorTracker.myOperator("==", a1537379265, 14, 91)))) {
            cf = false;
            Errors.__VERIFIER_error(38);
        }
        if ((((a894128990.equals("f")) && (a780728121.equals("i"))) && (OperatorTracker.myOperator("==", a1537379265, 13, 92)))) {
            cf = false;
            Errors.__VERIFIER_error(39);
        }
        if ((((a1360225377.equals("e")) && (OperatorTracker.myOperator("==", a71487061, 10, 93))) && (OperatorTracker.myOperator("==", a1537379265, 10, 94)))) {
            cf = false;
            Errors.__VERIFIER_error(40);
        }
        if ((((OperatorTracker.myOperator("==", a553986020, 11, 95)) && (a1360225377.equals("f"))) && (OperatorTracker.myOperator("==", a1537379265, 8, 96)))) {
            cf = false;
            Errors.__VERIFIER_error(41);
        }
        if ((((a729595174.equals("e")) && (OperatorTracker.myOperator("==", a168638684, 14, 97))) && (OperatorTracker.myOperator("==", a1537379265, 14, 98)))) {
            cf = false;
            Errors.__VERIFIER_error(42);
        }
        if ((((OperatorTracker.myOperator("==", a419289115, 10, 99)) && (a780728121.equals("g"))) && (OperatorTracker.myOperator("==", a1537379265, 13, 100)))) {
            cf = false;
            Errors.__VERIFIER_error(43);
        }
        if ((((OperatorTracker.myOperator("==", a547511656, 6, 101)) && (OperatorTracker.myOperator("==", a168638684, 8, 102))) && (OperatorTracker.myOperator("==", a1537379265, 14, 103)))) {
            cf = false;
            Errors.__VERIFIER_error(44);
        }
        if ((((OperatorTracker.myOperator("==", a419289115, 14, 104)) && (a780728121.equals("g"))) && (OperatorTracker.myOperator("==", a1537379265, 13, 105)))) {
            cf = false;
            Errors.__VERIFIER_error(45);
        }
        if ((((OperatorTracker.myOperator("==", a1392217129, 17, 106)) && (a1304806974.equals("i"))) && (OperatorTracker.myOperator("==", a1537379265, 15, 107)))) {
            cf = false;
            Errors.__VERIFIER_error(46);
        }
        if ((((a1367961664.equals("i")) && (a1304806974.equals("e"))) && (OperatorTracker.myOperator("==", a1537379265, 15, 108)))) {
            cf = false;
            Errors.__VERIFIER_error(47);
        }
        if ((((OperatorTracker.myOperator("==", a399501280, 9, 109)) && (a1304806974.equals("h"))) && (OperatorTracker.myOperator("==", a1537379265, 15, 110)))) {
            cf = false;
            Errors.__VERIFIER_error(48);
        }
        if ((((a278675587.equals("e")) && (a1360225377.equals("h"))) && (OperatorTracker.myOperator("==", a1537379265, 8, 111)))) {
            cf = false;
            Errors.__VERIFIER_error(49);
        }
        if ((((a1283870923.equals("h")) && (a894128990.equals("f"))) && (OperatorTracker.myOperator("==", a1537379265, 12, 112)))) {
            cf = false;
            Errors.__VERIFIER_error(50);
        }
        if ((((a729595174.equals("i")) && (OperatorTracker.myOperator("==", a168638684, 14, 113))) && (OperatorTracker.myOperator("==", a1537379265, 14, 114)))) {
            cf = false;
            Errors.__VERIFIER_error(51);
        }
        if ((((a512217640.equals("f")) && (OperatorTracker.myOperator("==", a1735071167, 12, 115))) && (OperatorTracker.myOperator("==", a1537379265, 11, 116)))) {
            cf = false;
            Errors.__VERIFIER_error(52);
        }
        if ((((OperatorTracker.myOperator("==", a399501280, 12, 117)) && (a1304806974.equals("h"))) && (OperatorTracker.myOperator("==", a1537379265, 15, 118)))) {
            cf = false;
            Errors.__VERIFIER_error(53);
        }
        if ((((a1353794423.equals("i")) && (OperatorTracker.myOperator("==", a919251806, 6, 119))) && (OperatorTracker.myOperator("==", a1537379265, 9, 120)))) {
            cf = false;
            Errors.__VERIFIER_error(54);
        }
        if ((((OperatorTracker.myOperator("==", a547511656, 11, 121)) && (OperatorTracker.myOperator("==", a919251806, 10, 122))) && (OperatorTracker.myOperator("==", a1537379265, 9, 123)))) {
            cf = false;
            Errors.__VERIFIER_error(55);
        }
        if ((((OperatorTracker.myOperator("==", a399501280, 8, 124)) && (a1304806974.equals("h"))) && (OperatorTracker.myOperator("==", a1537379265, 15, 125)))) {
            cf = false;
            Errors.__VERIFIER_error(56);
        }
        if ((((OperatorTracker.myOperator("==", a553986020, 10, 126)) && (a1360225377.equals("f"))) && (OperatorTracker.myOperator("==", a1537379265, 8, 127)))) {
            cf = false;
            Errors.__VERIFIER_error(57);
        }
        if ((((a1367961664.equals("h")) && (a1304806974.equals("e"))) && (OperatorTracker.myOperator("==", a1537379265, 15, 128)))) {
            cf = false;
            Errors.__VERIFIER_error(58);
        }
        if ((((OperatorTracker.myOperator("==", a1209890889, 6, 129)) && (OperatorTracker.myOperator("==", a1735071167, 13, 130))) && (OperatorTracker.myOperator("==", a1537379265, 11, 131)))) {
            cf = false;
            Errors.__VERIFIER_error(59);
        }
        if ((((OperatorTracker.myOperator("==", a823314445, 12, 132)) && (OperatorTracker.myOperator("==", a1735071167, 9, 133))) && (OperatorTracker.myOperator("==", a1537379265, 11, 134)))) {
            cf = false;
            Errors.__VERIFIER_error(60);
        }
        if ((((OperatorTracker.myOperator("==", a419289115, 12, 135)) && (OperatorTracker.myOperator("==", a168638684, 10, 136))) && (OperatorTracker.myOperator("==", a1537379265, 14, 137)))) {
            cf = false;
            Errors.__VERIFIER_error(61);
        }
        if ((((a1643269988.equals("g")) && (OperatorTracker.myOperator("==", a71487061, 13, 138))) && (OperatorTracker.myOperator("==", a1537379265, 10, 139)))) {
            cf = false;
            Errors.__VERIFIER_error(62);
        }
        if ((((a1353794423.equals("g")) && (OperatorTracker.myOperator("==", a168638684, 12, 140))) && (OperatorTracker.myOperator("==", a1537379265, 14, 141)))) {
            cf = false;
            Errors.__VERIFIER_error(63);
        }
        if ((((a894128990.equals("g")) && (a1304806974.equals("g"))) && (OperatorTracker.myOperator("==", a1537379265, 15, 142)))) {
            cf = false;
            Errors.__VERIFIER_error(64);
        }
        if ((((OperatorTracker.myOperator("==", a1575755525, 8, 143)) && (a894128990.equals("h"))) && (OperatorTracker.myOperator("==", a1537379265, 12, 144)))) {
            cf = false;
            Errors.__VERIFIER_error(65);
        }
        if ((((a572079232.equals("f")) && (OperatorTracker.myOperator("==", a919251806, 4, 145))) && (OperatorTracker.myOperator("==", a1537379265, 9, 146)))) {
            cf = false;
            Errors.__VERIFIER_error(66);
        }
        if ((((OperatorTracker.myOperator("==", a399501280, 13, 147)) && (a1304806974.equals("h"))) && (OperatorTracker.myOperator("==", a1537379265, 15, 148)))) {
            cf = false;
            Errors.__VERIFIER_error(67);
        }
        if ((((OperatorTracker.myOperator("==", a419289115, 13, 149)) && (OperatorTracker.myOperator("==", a168638684, 10, 150))) && (OperatorTracker.myOperator("==", a1537379265, 14, 151)))) {
            cf = false;
            Errors.__VERIFIER_error(68);
        }
        if ((((OperatorTracker.myOperator("==", a547511656, 12, 152)) && (OperatorTracker.myOperator("==", a168638684, 8, 153))) && (OperatorTracker.myOperator("==", a1537379265, 14, 154)))) {
            cf = false;
            Errors.__VERIFIER_error(69);
        }
        if ((((OperatorTracker.myOperator("==", a1392217129, 15, 155)) && (a1304806974.equals("i"))) && (OperatorTracker.myOperator("==", a1537379265, 15, 156)))) {
            cf = false;
            Errors.__VERIFIER_error(70);
        }
        if ((((OperatorTracker.myOperator("==", a1370900715, 9, 157)) && (a780728121.equals("e"))) && (OperatorTracker.myOperator("==", a1537379265, 13, 158)))) {
            cf = false;
            Errors.__VERIFIER_error(71);
        }
        if ((((OperatorTracker.myOperator("==", a1392217129, 17, 159)) && (a1360225377.equals("e"))) && (OperatorTracker.myOperator("==", a1537379265, 8, 160)))) {
            cf = false;
            Errors.__VERIFIER_error(72);
        }
        if ((((OperatorTracker.myOperator("==", a1575755525, 9, 161)) && (a894128990.equals("h"))) && (OperatorTracker.myOperator("==", a1537379265, 12, 162)))) {
            cf = false;
            Errors.__VERIFIER_error(73);
        }
        if ((((a76449788.equals("g")) && (OperatorTracker.myOperator("==", a71487061, 12, 163))) && (OperatorTracker.myOperator("==", a1537379265, 10, 164)))) {
            cf = false;
            Errors.__VERIFIER_error(74);
        }
        if ((((OperatorTracker.myOperator("==", a553209804, 6, 165)) && (OperatorTracker.myOperator("==", a919251806, 8, 166))) && (OperatorTracker.myOperator("==", a1537379265, 9, 167)))) {
            cf = false;
            Errors.__VERIFIER_error(75);
        }
        if ((((a1643269988.equals("i")) && (OperatorTracker.myOperator("==", a919251806, 5, 168))) && (OperatorTracker.myOperator("==", a1537379265, 9, 169)))) {
            cf = false;
            Errors.__VERIFIER_error(76);
        }
        if ((((OperatorTracker.myOperator("==", a823314445, 11, 170)) && (OperatorTracker.myOperator("==", a1735071167, 9, 171))) && (OperatorTracker.myOperator("==", a1537379265, 11, 172)))) {
            cf = false;
            Errors.__VERIFIER_error(77);
        }
        if ((((OperatorTracker.myOperator("==", a1023470345, 11, 173)) && (OperatorTracker.myOperator("==", a1735071167, 15, 174))) && (OperatorTracker.myOperator("==", a1537379265, 11, 175)))) {
            cf = false;
            Errors.__VERIFIER_error(78);
        }
        if ((((a894128990.equals("g")) && (a780728121.equals("h"))) && (OperatorTracker.myOperator("==", a1537379265, 13, 176)))) {
            cf = false;
            Errors.__VERIFIER_error(79);
        }
        if ((((a1643269988.equals("h")) && (OperatorTracker.myOperator("==", a71487061, 13, 177))) && (OperatorTracker.myOperator("==", a1537379265, 10, 178)))) {
            cf = false;
            Errors.__VERIFIER_error(80);
        }
        if ((((a1367961664.equals("e")) && (a1304806974.equals("e"))) && (OperatorTracker.myOperator("==", a1537379265, 15, 179)))) {
            cf = false;
            Errors.__VERIFIER_error(81);
        }
        if ((((a1283870923.equals("g")) && (a894128990.equals("f"))) && (OperatorTracker.myOperator("==", a1537379265, 12, 180)))) {
            cf = false;
            Errors.__VERIFIER_error(82);
        }
        if ((((OperatorTracker.myOperator("==", a2021551447, 7, 181)) && (OperatorTracker.myOperator("==", a71487061, 15, 182))) && (OperatorTracker.myOperator("==", a1537379265, 10, 183)))) {
            cf = false;
            Errors.__VERIFIER_error(83);
        }
        if ((((a894128990.equals("f")) && (OperatorTracker.myOperator("==", a1735071167, 14, 184))) && (OperatorTracker.myOperator("==", a1537379265, 11, 185)))) {
            cf = false;
            Errors.__VERIFIER_error(84);
        }
        if ((((OperatorTracker.myOperator("==", a1565409750, 9, 186)) && (OperatorTracker.myOperator("==", a168638684, 9, 187))) && (OperatorTracker.myOperator("==", a1537379265, 14, 188)))) {
            cf = false;
            Errors.__VERIFIER_error(85);
        }
        if ((((a1283870923.equals("e")) && (a894128990.equals("f"))) && (OperatorTracker.myOperator("==", a1537379265, 12, 189)))) {
            cf = false;
            Errors.__VERIFIER_error(86);
        }
        if ((((a990334020.equals("e")) && (OperatorTracker.myOperator("==", a71487061, 17, 190))) && (OperatorTracker.myOperator("==", a1537379265, 10, 191)))) {
            cf = false;
            Errors.__VERIFIER_error(87);
        }
        if ((((a512217640.equals("h")) && (OperatorTracker.myOperator("==", a1735071167, 12, 192))) && (OperatorTracker.myOperator("==", a1537379265, 11, 193)))) {
            cf = false;
            Errors.__VERIFIER_error(88);
        }
        if ((((a1367961664.equals("f")) && (OperatorTracker.myOperator("==", a168638684, 13, 194))) && (OperatorTracker.myOperator("==", a1537379265, 14, 195)))) {
            cf = false;
            Errors.__VERIFIER_error(89);
        }
        if ((((a894128990.equals("i")) && (a780728121.equals("h"))) && (OperatorTracker.myOperator("==", a1537379265, 13, 196)))) {
            cf = false;
            Errors.__VERIFIER_error(90);
        }
        if ((((a1643269988.equals("i")) && (OperatorTracker.myOperator("==", a71487061, 14, 197))) && (OperatorTracker.myOperator("==", a1537379265, 10, 198)))) {
            cf = false;
            Errors.__VERIFIER_error(91);
        }
        if ((((a1643269988.equals("e")) && (OperatorTracker.myOperator("==", a71487061, 13, 199))) && (OperatorTracker.myOperator("==", a1537379265, 10, 200)))) {
            cf = false;
            Errors.__VERIFIER_error(92);
        }
        if ((((a894128990.equals("h")) && (a780728121.equals("i"))) && (OperatorTracker.myOperator("==", a1537379265, 13, 201)))) {
            cf = false;
            Errors.__VERIFIER_error(93);
        }
        if ((((OperatorTracker.myOperator("==", a1023470345, 12, 202)) && (OperatorTracker.myOperator("==", a1735071167, 10, 203))) && (OperatorTracker.myOperator("==", a1537379265, 11, 204)))) {
            cf = false;
            Errors.__VERIFIER_error(94);
        }
        if ((((OperatorTracker.myOperator("==", a1370900715, 13, 205)) && (a780728121.equals("e"))) && (OperatorTracker.myOperator("==", a1537379265, 13, 206)))) {
            cf = false;
            Errors.__VERIFIER_error(95);
        }
        if ((((a1353794423.equals("h")) && (OperatorTracker.myOperator("==", a168638684, 12, 207))) && (OperatorTracker.myOperator("==", a1537379265, 14, 208)))) {
            cf = false;
            Errors.__VERIFIER_error(96);
        }
        if ((((OperatorTracker.myOperator("==", a1370900715, 10, 209)) && (a780728121.equals("e"))) && (OperatorTracker.myOperator("==", a1537379265, 13, 210)))) {
            cf = false;
            Errors.__VERIFIER_error(97);
        }
        if ((((OperatorTracker.myOperator("==", a553986020, 8, 211)) && (a1360225377.equals("f"))) && (OperatorTracker.myOperator("==", a1537379265, 8, 212)))) {
            cf = false;
            Errors.__VERIFIER_error(98);
        }
        if ((((OperatorTracker.myOperator("==", a547511656, 8, 213)) && (OperatorTracker.myOperator("==", a919251806, 10, 214))) && (OperatorTracker.myOperator("==", a1537379265, 9, 215)))) {
            cf = false;
            Errors.__VERIFIER_error(99);
        }
    }

    private void calculateOutputm46(String input) {
        if (((((a1360225377.equals("e")) && ((OperatorTracker.myOperator("==", a1537379265, 8, 216)) && cf)) && (OperatorTracker.myOperator("==", a1392217129, 14, 217))) && (input.equals("J")))) {
            cf = false;
            a780728121 = "i";
            a894128990 = "e";
            a1537379265 = 13;
            OperatorTracker.output("Y");
        }
        if (((((cf && (a1360225377.equals("e"))) && (input.equals("A"))) && (OperatorTracker.myOperator("==", a1537379265, 8, 218))) && (OperatorTracker.myOperator("==", a1392217129, 14, 219)))) {
            cf = false;
            a1537379265 = 10;
            a1643269988 = "g";
            a71487061 = 13;
            OperatorTracker.output("R");
        }
    }

    private void calculateOutputm1(String input) {
        if ((cf && (OperatorTracker.myOperator("==", a1392217129, 14, 220)))) {
            calculateOutputm46(input);
        }
    }

    private void calculateOutputm51(String input) {
        if (((((input.equals("B")) && ((a1360225377.equals("g")) && cf)) && (a61085062.equals("f"))) && (OperatorTracker.myOperator("==", a1537379265, 8, 221)))) {
            cf = false;
            a1367961664 = "g";
            a1304806974 = "e";
            a1537379265 = 15;
            OperatorTracker.output("W");
        }
        if (((input.equals("F")) && ((a1360225377.equals("g")) && ((a61085062.equals("f")) && ((OperatorTracker.myOperator("==", a1537379265, 8, 222)) && cf))))) {
            cf = false;
            a894128990 = "h";
            a1537379265 = 11;
            a1735071167 = 14;
            OperatorTracker.output("Q");
        }
        if (((input.equals("H")) && (((OperatorTracker.myOperator("!=", a1537379265, 8, 223)) && ((a61085062.equals("f")) && cf)) && (a1360225377.equals("g"))))) {
            cf = false;
            a1367961664 = "g";
            a1304806974 = "e";
            a1537379265 = 15;
            OperatorTracker.output("W");
        }
        if (((((cf && (OperatorTracker.myOperator("==", a1537379265, 8, 224))) && (a1360225377.equals("g"))) && (a61085062.equals("f"))) && (input.equals("E")))) {
            cf = false;
            a1537379265 = 11;
            a1735071167 = 11;
            a521273111 = 2;
            OperatorTracker.output("W");
        }
    }

    private void calculateOutputm3(String input) {
        if ((cf && (a61085062.equals("f")))) {
            calculateOutputm51(input);
        }
    }

    private void calculateOutputm54(String input) {
        if (((a278675587.equals("f")) && ((input.equals("D")) && ((OperatorTracker.myOperator("==", a1537379265, 8, 225)) && ((a1360225377.equals("h")) && cf))))) {
            cf = false;
            a780728121 = "i";
            a894128990 = "e";
            a1537379265 = 13;
            OperatorTracker.output("Y");
        }
        if (((a1360225377.equals("h")) && (((a278675587.equals("f")) && (cf && (input.equals("A")))) && (OperatorTracker.myOperator("==", a1537379265, 8, 226))))) {
            cf = false;
            a71487061 = 15;
            a1537379265 = 10;
            a2021551447 = 5;
            OperatorTracker.output("U");
        }
    }

    private void calculateOutputm55(String input) {
        if ((((a278675587.equals("h")) && (((OperatorTracker.myOperator("==", a1537379265, 8, 227)) && cf) && (input.equals("F")))) && (a1360225377.equals("h")))) {
            cf = false;
            a1537379265 = 14;
            a1353794423 = "i";
            a168638684 = 12;
            OperatorTracker.output("R");
        }
        if ((((input.equals("B")) && ((a1360225377.equals("h")) && ((OperatorTracker.myOperator("==", a1537379265, 8, 228)) && cf))) && (a278675587.equals("h")))) {
            cf = false;
            a894128990 = "f";
            a780728121 = "h";
            a1537379265 = 13;
            OperatorTracker.output("Z");
        }
    }

    private void calculateOutputm4(String input) {
        if ((cf && (a278675587.equals("f")))) {
            calculateOutputm54(input);
        }
        if ((cf && (a278675587.equals("h")))) {
            calculateOutputm55(input);
        }
    }

    private void calculateOutputm62(String input) {
        if (((OperatorTracker.myOperator("==", a553209804, 9, 229)) && ((input.equals("D")) && ((OperatorTracker.myOperator("==", a919251806, 8, 230)) && ((OperatorTracker.myOperator("==", a1537379265, 9, 231)) && cf))))) {
            cf = false;
            a894128990 = "e";
            a780728121 = "i";
            a1537379265 = 13;
            OperatorTracker.output("Y");
        }
        if (((((OperatorTracker.myOperator("==", a553209804, 9, 232)) && ((OperatorTracker.myOperator("==", a919251806, 8, 233)) && cf)) && (OperatorTracker.myOperator("==", a1537379265, 9, 234))) && (input.equals("J")))) {
            cf = false;
            a1367961664 = "g";
            a1304806974 = "e";
            a1537379265 = 15;
            OperatorTracker.output("W");
        }
        if (((OperatorTracker.myOperator("==", a919251806, 8, 235)) && (((cf && (input.equals("H"))) && (OperatorTracker.myOperator("==", a1537379265, 9, 236))) && (OperatorTracker.myOperator("==", a553209804, 9, 237))))) {
            cf = false;
            a512217640 = "f";
            a1537379265 = 11;
            a1735071167 = 12;
            OperatorTracker.output("Y");
        }
    }

    private void calculateOutputm8(String input) {
        if (((OperatorTracker.myOperator("==", a553209804, 9, 238)) && cf)) {
            calculateOutputm62(input);
        }
    }

    private void calculateOutputm65(String input) {
        if (((((cf && (a2007907442.equals("g"))) && (input.equals("I"))) && (OperatorTracker.myOperator("==", a919251806, 11, 239))) && (OperatorTracker.myOperator("==", a1537379265, 9, 240)))) {
            cf = false;
            a1304806974 = "e";
            a1367961664 = "g";
            a1537379265 = 15;
            OperatorTracker.output("Q");
        }
        if (((OperatorTracker.myOperator("==", a1537379265, 9, 241)) && (((cf && (a2007907442.equals("g"))) && (input.equals("C"))) && (OperatorTracker.myOperator("==", a919251806, 11, 242))))) {
            cf = false;
            a1537379265 = 13;
            a780728121 = "e";
            a1370900715 = 13;
            OperatorTracker.output("X");
        }
    }

    private void calculateOutputm10(String input) {
        if (((a2007907442.equals("g")) && cf)) {
            calculateOutputm65(input);
        }
    }

    private void calculateOutputm85(String input) {
        if (((OperatorTracker.myOperator("==", a71487061, 17, 243)) && (((a990334020.equals("h")) && ((OperatorTracker.myOperator("==", a1537379265, 10, 244)) && cf)) && (input.equals("F"))))) {
            cf = false;
            a1353794423 = "i";
            a1537379265 = 14;
            a168638684 = 12;
            OperatorTracker.output("R");
        }
        if (((OperatorTracker.myOperator("==", a1537379265, 10, 245)) && ((a990334020.equals("h")) && ((input.equals("J")) && (cf && (OperatorTracker.myOperator("==", a71487061, 17, 246))))))) {
            cf = false;
            a1304806974 = "h";
            a894128990 = "e";
            a1537379265 = 12;
            OperatorTracker.output("Z");
        }
        if (((((cf && (OperatorTracker.myOperator("==", a1537379265, 10, 247))) && (a990334020.equals("h"))) && (OperatorTracker.myOperator("==", a71487061, 17, 248))) && (input.equals("A")))) {
            cf = false;
            a1537379265 = 9;
            a919251806 = 8;
            a553209804 = 6;
            OperatorTracker.output("U");
        }
    }

    private void calculateOutputm17(String input) {
        if (((a990334020.equals("h")) && cf)) {
            calculateOutputm85(input);
        }
    }

    private void calculateOutputm88(String input) {
        if ((((OperatorTracker.myOperator("==", a1537379265, 11, 249)) && ((cf && (OperatorTracker.myOperator("==", a823314445, 15, 250))) && (OperatorTracker.myOperator("==", a1735071167, 9, 251)))) && (input.equals("A")))) {
            cf = false;
            a1735071167 = 11;
            a521273111 = 4;
            OperatorTracker.output("Z");
        }
        if (((OperatorTracker.myOperator("==", a1735071167, 9, 252)) && (((cf && (OperatorTracker.myOperator("==", a823314445, 15, 253))) && (input.equals("B"))) && (OperatorTracker.myOperator("==", a1537379265, 11, 254))))) {
            cf = false;
            a1304806974 = "g";
            a894128990 = "i";
            a1537379265 = 15;
            OperatorTracker.output("X");
        }
        if (((input.equals("H")) && ((((OperatorTracker.myOperator("==", a1735071167, 9, 255)) && cf) && (OperatorTracker.myOperator("==", a823314445, 15, 256))) && (OperatorTracker.myOperator("==", a1537379265, 11, 257))))) {
            cf = false;
            a1643269988 = "i";
            a1537379265 = 10;
            a71487061 = 14;
            OperatorTracker.output("R");
        }
    }

    private void calculateOutputm18(String input) {
        if (((OperatorTracker.myOperator("==", a823314445, 15, 258)) && cf)) {
            calculateOutputm88(input);
        }
    }

    private void calculateOutputm94(String input) {
        if (((input.equals("A")) && ((OperatorTracker.myOperator("==", a521273111, 4, 259)) && ((OperatorTracker.myOperator("==", a1537379265, 11, 260)) && (cf && (OperatorTracker.myOperator("==", a1735071167, 11, 261))))))) {
            cf = false;
            a1537379265 = 14;
            a168638684 = 15;
            a1824730115 = 12;
            OperatorTracker.output("X");
        }
        if (((OperatorTracker.myOperator("==", a521273111, 4, 262)) && ((input.equals("G")) && ((cf && (OperatorTracker.myOperator("==", a1537379265, 11, 263))) && (OperatorTracker.myOperator("==", a1735071167, 11, 264)))))) {
            cf = false;
            a1735071167 = 9;
            a823314445 = 15;
            OperatorTracker.output("Q");
        }
        if ((((OperatorTracker.myOperator("==", a1537379265, 11, 265)) && (((OperatorTracker.myOperator("==", a1735071167, 11, 266)) && cf) && (input.equals("J")))) && (OperatorTracker.myOperator("==", a521273111, 4, 267)))) {
            cf = false;
            a1537379265 = 10;
            a990334020 = "h";
            a71487061 = 17;
            OperatorTracker.output("Z");
        }
        if (((OperatorTracker.myOperator("==", a1537379265, 11, 268)) && (((OperatorTracker.myOperator("==", a1735071167, 11, 269)) && ((OperatorTracker.myOperator("==", a521273111, 4, 270)) && cf)) && (input.equals("D"))))) {
            cf = false;
            a1537379265 = 15;
            a1304806974 = "i";
            a1392217129 = 10;
            OperatorTracker.output("W");
        }
    }

    private void calculateOutputm95(String input) {
        if (((OperatorTracker.myOperator("==", a1735071167, 11, 271)) && ((OperatorTracker.myOperator("==", a1537379265, 11, 272)) && ((OperatorTracker.myOperator("==", a521273111, 8, 273)) && (cf && (input.equals("J"))))))) {
            cf = false;
            a168638684 = 15;
            a1537379265 = 14;
            a1824730115 = 12;
            OperatorTracker.output("X");
        }
        if ((((input.equals("C")) && (((OperatorTracker.myOperator("==", a1537379265, 11, 274)) && cf) && (OperatorTracker.myOperator("==", a1735071167, 11, 275)))) && (OperatorTracker.myOperator("==", a521273111, 8, 276)))) {
            cf = false;
            a1283870923 = "g";
            a894128990 = "f";
            a1537379265 = 12;
            OperatorTracker.output("Z");
        }
    }

    private void calculateOutputm20(String input) {
        if (((OperatorTracker.myOperator("==", a521273111, 4, 277)) && cf)) {
            calculateOutputm94(input);
        }
        if (((OperatorTracker.myOperator("==", a521273111, 8, 278)) && cf)) {
            calculateOutputm95(input);
        }
    }

    private void calculateOutputm104(String input) {
        if (((input.equals("A")) && ((a894128990.equals("h")) && (((OperatorTracker.myOperator("==", a1735071167, 14, 279)) && cf) && (OperatorTracker.myOperator("==", a1537379265, 11, 280)))))) {
            cf = false;
            a1360225377 = "g";
            a61085062 = "f";
            a1537379265 = 8;
            OperatorTracker.output("Q");
        }
        if (((a894128990.equals("h")) && (((OperatorTracker.myOperator("==", a1735071167, 14, 281)) && ((input.equals("I")) && cf)) && (OperatorTracker.myOperator("==", a1537379265, 11, 282))))) {
            cf = false;
            a278675587 = "f";
            a1360225377 = "h";
            a1537379265 = 8;
            OperatorTracker.output("R");
        }
        if (((OperatorTracker.myOperator("==", a1537379265, 11, 283)) && (((a894128990.equals("h")) && ((input.equals("J")) && cf)) && (OperatorTracker.myOperator("==", a1735071167, 14, 284))))) {
            cf = false;
            a894128990 = "e";
            a780728121 = "i";
            a1537379265 = 13;
            OperatorTracker.output("Y");
        }
        if (((((cf && (a894128990.equals("h"))) && (input.equals("B"))) && (OperatorTracker.myOperator("==", a1537379265, 11, 285))) && (OperatorTracker.myOperator("==", a1735071167, 14, 286)))) {
            cf = false;
            a780728121 = "i";
            a894128990 = "f";
            a1537379265 = 13;
            OperatorTracker.output("R");
        }
    }

    private void calculateOutputm23(String input) {
        if (((a894128990.equals("h")) && cf)) {
            calculateOutputm104(input);
        }
    }

    private void calculateOutputm107(String input) {
        if (((OperatorTracker.myOperator("==", a1537379265, 12, 287)) && ((((a1304806974.equals("h")) && cf) && (input.equals("F"))) && (a894128990.equals("e"))))) {
            cf = false;
            OperatorTracker.output("Z");
        }
        if (((input.equals("H")) && ((OperatorTracker.myOperator("==", a1537379265, 12, 288)) && ((a894128990.equals("e")) && ((a1304806974.equals("h")) && cf))))) {
            cf = false;
            a990334020 = "h";
            a1537379265 = 10;
            a71487061 = 17;
            OperatorTracker.output("S");
        }
        if (((a894128990.equals("e")) && (((OperatorTracker.myOperator("==", a1537379265, 12, 289)) && ((a1304806974.equals("h")) && cf)) && (input.equals("G"))))) {
            cf = false;
            a1537379265 = 14;
            a729595174 = "e";
            a168638684 = 14;
            OperatorTracker.output("V");
        }
    }

    private void calculateOutputm25(String input) {
        if (((a1304806974.equals("h")) && cf)) {
            calculateOutputm107(input);
        }
    }

    private void calculateOutputm126(String input) {
        if ((((a780728121.equals("i")) && ((input.equals("E")) && (cf && (a894128990.equals("e"))))) && (OperatorTracker.myOperator("==", a1537379265, 13, 290)))) {
            cf = false;
            a1537379265 = 14;
            a168638684 = 10;
            a419289115 = 7;
            OperatorTracker.output("X");
        }
        if ((((((input.equals("F")) && cf) && (a894128990.equals("e"))) && (OperatorTracker.myOperator("==", a1537379265, 13, 291))) && (a780728121.equals("i")))) {
            cf = false;
            a61085062 = "f";
            a1360225377 = "g";
            a1537379265 = 8;
            OperatorTracker.output("Q");
        }
        if (((a894128990.equals("e")) && ((a780728121.equals("i")) && ((input.equals("I")) && (cf && (OperatorTracker.myOperator("==", a1537379265, 13, 292))))))) {
            cf = false;
            a278675587 = "h";
            a1360225377 = "h";
            a1537379265 = 8;
            OperatorTracker.output("S");
        }
        if (((a780728121.equals("i")) && ((input.equals("H")) && (((OperatorTracker.myOperator("==", a1537379265, 13, 293)) && cf) && (a894128990.equals("e")))))) {
            cf = false;
            a76449788 = "e";
            a1537379265 = 10;
            a71487061 = 12;
            OperatorTracker.output("W");
        }
    }

    private void calculateOutputm32(String input) {
        if (((a894128990.equals("e")) && cf)) {
            calculateOutputm126(input);
        }
    }

    private void calculateOutputm134(String input) {
        if (((OperatorTracker.myOperator("==", a168638684, 10, 294)) && (((cf && (input.equals("B"))) && (OperatorTracker.myOperator("==", a419289115, 7, 295))) && (OperatorTracker.myOperator("==", a1537379265, 14, 296))))) {
            cf = false;
            a168638684 = 15;
            a1824730115 = 12;
            OperatorTracker.output("X");
        }
        if ((((OperatorTracker.myOperator("==", a168638684, 10, 297)) && (((OperatorTracker.myOperator("==", a1537379265, 14, 298)) && cf) && (OperatorTracker.myOperator("==", a419289115, 7, 299)))) && (input.equals("H")))) {
            cf = false;
            a919251806 = 8;
            a1537379265 = 9;
            a553209804 = 9;
            OperatorTracker.output("O");
        }
        if (((OperatorTracker.myOperator("==", a419289115, 7, 300)) && ((OperatorTracker.myOperator("==", a168638684, 10, 301)) && (((input.equals("D")) && cf) && (OperatorTracker.myOperator("==", a1537379265, 14, 302)))))) {
            cf = false;
            a1537379265 = 11;
            a1735071167 = 10;
            a1023470345 = 12;
            OperatorTracker.output("X");
        }
    }

    private void calculateOutputm35(String input) {
        if ((cf && (OperatorTracker.myOperator("==", a419289115, 7, 303)))) {
            calculateOutputm134(input);
        }
    }

    private void calculateOutputm142(String input) {
        if (((input.equals("G")) && ((OperatorTracker.myOperator("==", a1537379265, 14, 304)) && ((OperatorTracker.myOperator("==", a168638684, 12, 305)) && ((a1353794423.equals("i")) && cf))))) {
            cf = false;
            OperatorTracker.output("R");
        }
        if (((OperatorTracker.myOperator("==", a168638684, 12, 306)) && (((input.equals("F")) && (cf && (OperatorTracker.myOperator("==", a1537379265, 14, 307)))) && (a1353794423.equals("i"))))) {
            cf = false;
            a780728121 = "i";
            a894128990 = "h";
            a1537379265 = 13;
            OperatorTracker.output("R");
        }
    }

    private void calculateOutputm37(String input) {
        if ((cf && (a1353794423.equals("i")))) {
            calculateOutputm142(input);
        }
    }

    private void calculateOutputm147(String input) {
        if (((OperatorTracker.myOperator("==", a1824730115, 12, 308)) && (((OperatorTracker.myOperator("==", a168638684, 15, 309)) && ((input.equals("F")) && cf)) && (OperatorTracker.myOperator("==", a1537379265, 14, 310))))) {
            cf = false;
            a1537379265 = 11;
            a1735071167 = 11;
            a521273111 = 4;
            OperatorTracker.output("Z");
        }
        if (((OperatorTracker.myOperator("==", a168638684, 15, 311)) && ((OperatorTracker.myOperator("==", a1537379265, 14, 312)) && ((OperatorTracker.myOperator("==", a1824730115, 12, 313)) && (cf && (input.equals("I"))))))) {
            cf = false;
            a894128990 = "i";
            a1304806974 = "g";
            a1537379265 = 15;
            OperatorTracker.output("W");
        }
        if ((((((OperatorTracker.myOperator("==", a1537379265, 14, 314)) && cf) && (input.equals("J"))) && (OperatorTracker.myOperator("==", a168638684, 15, 315))) && (OperatorTracker.myOperator("==", a1824730115, 12, 316)))) {
            cf = false;
            a1353794423 = "i";
            a168638684 = 12;
            OperatorTracker.output("U");
        }
        if (((((OperatorTracker.myOperator("==", a1537379265, 14, 317)) && ((OperatorTracker.myOperator("==", a1824730115, 12, 318)) && cf)) && (input.equals("H"))) && (OperatorTracker.myOperator("==", a168638684, 15, 319)))) {
            cf = false;
            a1537379265 = 11;
            a1735071167 = 9;
            a823314445 = 12;
            OperatorTracker.output("Y");
        }
    }

    private void calculateOutputm148(String input) {
        if ((((OperatorTracker.myOperator("==", a168638684, 15, 320)) && ((cf && (input.equals("B"))) && (OperatorTracker.myOperator("==", a1824730115, 13, 321)))) && (OperatorTracker.myOperator("==", a1537379265, 14, 322)))) {
            cf = false;
            a1824730115 = 12;
            OperatorTracker.output("V");
        }
        if (((input.equals("E")) && (((cf && (OperatorTracker.myOperator("==", a1537379265, 14, 323))) && (OperatorTracker.myOperator("==", a1824730115, 13, 324))) && (OperatorTracker.myOperator("==", a168638684, 15, 325))))) {
            cf = false;
            a1537379265 = 9;
            a2007907442 = "g";
            a919251806 = 11;
            OperatorTracker.output("V");
        }
        if (((OperatorTracker.myOperator("==", a1537379265, 14, 326)) && ((((OperatorTracker.myOperator("==", a168638684, 15, 327)) && cf) && (OperatorTracker.myOperator("==", a1824730115, 13, 328))) && (input.equals("F"))))) {
            cf = false;
            a1360225377 = "e";
            a1537379265 = 8;
            a1392217129 = 14;
            OperatorTracker.output("U");
        }
    }

    private void calculateOutputm40(String input) {
        if (((OperatorTracker.myOperator("==", a1824730115, 12, 329)) && cf)) {
            calculateOutputm147(input);
        }
        if (((OperatorTracker.myOperator("==", a1824730115, 13, 330)) && cf)) {
            calculateOutputm148(input);
        }
    }

    private void calculateOutputm151(String input) {
        if ((((((OperatorTracker.myOperator("==", a1537379265, 15, 331)) && cf) && (input.equals("D"))) && (a1304806974.equals("e"))) && (a1367961664.equals("g")))) {
            cf = false;
            OperatorTracker.output("W");
        }
        if (((OperatorTracker.myOperator("==", a1537379265, 15, 332)) && ((input.equals("F")) && ((a1304806974.equals("e")) && (cf && (a1367961664.equals("g"))))))) {
            cf = false;
            a1304806974 = "h";
            a399501280 = 9;
            OperatorTracker.output("X");
        }
    }

    private void calculateOutputm41(String input) {
        if (((a1367961664.equals("g")) && cf)) {
            calculateOutputm151(input);
        }
    }

    private void calculateOutputm155(String input) {
        if ((((a894128990.equals("i")) && ((input.equals("B")) && ((a1304806974.equals("g")) && cf))) && (OperatorTracker.myOperator("==", a1537379265, 15, 333)))) {
            cf = false;
            a1735071167 = 11;
            a1537379265 = 11;
            a521273111 = 8;
            OperatorTracker.output("R");
        }
        if (((((cf && (OperatorTracker.myOperator("==", a1537379265, 15, 334))) && (a894128990.equals("i"))) && (input.equals("D"))) && (a1304806974.equals("g")))) {
            cf = false;
            OperatorTracker.output("X");
        }
        if (((input.equals("E")) && ((a894128990.equals("i")) && ((OperatorTracker.myOperator("==", a1537379265, 15, 335)) && (cf && (a1304806974.equals("g"))))))) {
            cf = false;
            a76449788 = "g";
            a1537379265 = 10;
            a71487061 = 12;
            OperatorTracker.output("Z");
        }
    }

    private void calculateOutputm42(String input) {
        if ((cf && (a894128990.equals("i")))) {
            calculateOutputm155(input);
        }
    }

    public void calculateOutput(String input) {
        cf = true;
        if (((OperatorTracker.myOperator("==", a1537379265, 8, 336)) && cf)) {
            if ((cf && (a1360225377.equals("e")))) {
                calculateOutputm1(input);
            }
            if ((cf && (a1360225377.equals("g")))) {
                calculateOutputm3(input);
            }
            if (((a1360225377.equals("h")) && cf)) {
                calculateOutputm4(input);
            }
        }
        if (((OperatorTracker.myOperator("==", a1537379265, 9, 337)) && cf)) {
            if (((OperatorTracker.myOperator("==", a919251806, 8, 338)) && cf)) {
                calculateOutputm8(input);
            }
            if (((OperatorTracker.myOperator("==", a919251806, 11, 339)) && cf)) {
                calculateOutputm10(input);
            }
        }
        if (((OperatorTracker.myOperator("!=", a1537379265, 10, 340)) && cf)) {
            if (((OperatorTracker.myOperator("==", a71487061, 17, 341)) && cf)) {
                calculateOutputm17(input);
            }
        }
        if ((cf && (OperatorTracker.myOperator("==", a1537379265, 11, 342)))) {
            if ((cf && (OperatorTracker.myOperator("==", a1735071167, 9, 343)))) {
                calculateOutputm18(input);
            }
            if (((OperatorTracker.myOperator("==", a1735071167, 11, 344)) && cf)) {
                calculateOutputm20(input);
            }
            if (((OperatorTracker.myOperator("==", a1735071167, 14, 345)) && cf)) {
                calculateOutputm23(input);
            }
        }
        if (((OperatorTracker.myOperator("==", a1537379265, 12, 346)) && cf)) {
            if (((a894128990.equals("e")) && cf)) {
                calculateOutputm25(input);
            }
        }
        if (((OperatorTracker.myOperator("==", a1537379265, 13, 347)) && cf)) {
            if (((a780728121.equals("i")) && cf)) {
                calculateOutputm32(input);
            }
        }
        if (((OperatorTracker.myOperator("==", a1537379265, 14, 348)) && cf)) {
            if (((OperatorTracker.myOperator("==", a168638684, 10, 349)) && cf)) {
                calculateOutputm35(input);
            }
            if (((OperatorTracker.myOperator("!=", a168638684, 12, 350)) && cf)) {
                calculateOutputm37(input);
            }
            if (((OperatorTracker.myOperator("==", a168638684, 15, 351)) && cf)) {
                calculateOutputm40(input);
            }
        }
        if (((OperatorTracker.myOperator("==", a1537379265, 15, 352)) && cf)) {
            if (((a1304806974.equals("e")) && cf)) {
                calculateOutputm41(input);
            }
            if ((cf && (a1304806974.equals("g")))) {
                calculateOutputm42(input);
            }
        }
        errorCheck();
        if (cf)
            throw new IllegalArgumentException("Current state has no transition for this input!");
    }

    public static void main(String[] args) throws Exception {
        // init system and input reader
        Problem11 eca = new Problem11();
        String[] operators = { "==", "==", "==", "==", "==", "==", "==", "==", "!=", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "!=", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "!=", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "!=", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "==", "!=", "==", "==", "==", "==", "==", "==", "==", "==", "==", "!=", "==", "==" };
        OperatorTracker.run(operators, eca);
    }

    public String[] sequence;

    public Void call() {
        Problem11 cp = new Problem11();
        for (String s : sequence) {
            try {
                cp.calculateOutput(s);
            } catch (Exception e) {
                PatchingLab.output("Invalid input: " + e.getMessage());
            }
        }
        return null;
    }

    public void setSequence(String[] trace) {
        sequence = trace;
    }
}

