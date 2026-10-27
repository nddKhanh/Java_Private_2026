package Thanh.src;
import java.util.Scanner;
public class HitShop {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        double VAT_Rate = 0.10 , SHIPPING_FEE = 20000;


        String productName;
        do {
            System.out.print(" Tên mặt hàng : ");
            productName = sc.nextLine();
            if (productName.isEmpty()){
                System.out.print(" Vui long nhap lai ten mat hang . Khong de trong !");
            }
        } while (productName.isEmpty());

        double price = 0;
        while (price <= 0){
            System.out.print(" gia cua mat hang :");
            price = sc.nextDouble();
            if (price <=0 ){
                System.out.print(" Vui long nhap lai gia ( khong nhap gia am hoac bang 0 ) !");
            }
        }

        int count = 0;
        do {
            System.out.print("so luong mat  hang : ");
            count = sc.nextInt();
            if (count <= 0 ){
                System.out.print(" Nhap lai so luong !");
            }
        } while ( count <= 0);

        double TotalPrice = price * count;
        double TotalPrice2 =TotalPrice+  TotalPrice * VAT_Rate +  SHIPPING_FEE;

        System.out.println(" Ten mat hang : " + productName) ;
        System.out.println(" Gia cua hang : " + price);
        System.out.println(" So luong mat hang : " + count);
        System.out.println(" Tong tien hang mua : " + TotalPrice);
        System.out.println("Tong tien sau khi dinh phi dich vu : " + TotalPrice2 );

        sc.close();
    }
}


