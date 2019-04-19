/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author HP
 */
public class Assignment {
    static Connection con = null;
    static Statement stmt;
    static int funds=0;
    static int TotalEarning=0;
    static String Customer_name;
    /**
     * @param args the command line arguments
     */
    void init(){
        try {  
            stmt = (Statement) con.createStatement();
            String st = "CREATE TABLE IF NOT EXISTS PRODUCT "
                    + "(id INTEGER not NULL,"
                    + " name VARCHAR(255) UNIQUE,"
                    + " category VARCHAR(255),"
                    + " subcategory VARCHAR(255),"
                    + " price INTEGER,"
                    + " units INTEGER,"
                    + "PRIMARY KEY(id)) ";
            stmt.executeUpdate(st);
            st = "CREATE TABLE IF NOT EXISTS CART "+
                "(customername VARCHAR(255),"
                    +"name VARCHAR(255),"
                    +"quantity INTEGER)";
            stmt.execute(st);
        } catch (SQLException ex) {
            Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    void insert(String path,Product product){
        String cat[] = path.split(">");
        try {
            String st = "insert into PRODUCT(id,name,category,subcategory,price,units)"
                    + " values(?,?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(st);
            ps.setInt(1, product.id);
            ps.setString(2,product.name);
            ps.setString(3, cat[0]);
            ps.setString(4, cat[1]);
            ps.setInt(5, product.price);
            ps.setInt(6, product.units);
            ps.execute();
        } catch (SQLException ex) {
            //Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
            //System.out.print(ex.getErrorCode());
            if(ex.getErrorCode()==1062){
                System.out.println("ID already exists");
            }
        }   
    }
    
    void delete(String s){
        String t[] = s.split(">");
        if(t.length==2){
            String st = "delete from PRODUCT where subcategory = ?";
            PreparedStatement  ps = null;
            try {
                ps = con.prepareStatement(st);
            } catch (SQLException ex) {
                Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                ps.setString(1, t[1]);
            } catch (SQLException ex) {
                Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println(ex.getErrorCode());
            }
            try {
                ps.execute();
            } catch (SQLException ex) {
                Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println(ex.getErrorCode());
            }
        }else{
            String st = "delete from PRODUCT where name = ?";
            PreparedStatement  ps = null;
            try {
                ps = con.prepareStatement(st);
            } catch (SQLException ex) {
                Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                ps.setString(1, t[2]);
            } catch (SQLException ex) {
                Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println(ex.getErrorCode());
            }
            try {
                ps.execute();
            } catch (SQLException ex) {
                Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println(ex.getErrorCode());
            }
        }
    }
    Product search(String name){
        Product p=null;
        String s = "Select * from PRODUCT where name = ?";
        try {
            PreparedStatement ps = con.prepareStatement(s);
            ps.setString(1,name);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                p = new Product(rs.getInt("id"),rs.getString("name"),rs.getInt("price"),rs.getInt("units"));
                //System.out.println(rs.getString("category")+">"+rs.getString("subcategory"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
        }
        return p;
    }
    
    void update()
    {
        Scanner in = new Scanner(System.in);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter the name of the product to be updated");
        String name = null;
        try {
            name = br.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
        }
        Product p = search(name);
        System.out.println("What do you want to modify? (1 for price 2 for units)");
        int d = in.nextInt();
        int x=0;
        if(d == 1){
            System.out.println("Enter the new price");
            x = in.nextInt();
            try {
                PreparedStatement ps = con.prepareStatement("update PRODUCT set price = ? where name = ?");
                ps.setInt(1,x);
                ps.setString(2, name);
                ps.execute();
            } catch (SQLException ex) {
                Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            System.out.println("Enter the new units");
            x = in.nextInt();
            try {
                PreparedStatement ps = con.prepareStatement("update PRODUCT set units = ? where name = ?");
                ps.setInt(1,x);
                ps.setString(2, name);
                ps.execute();
            } catch (SQLException ex) {
                Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    void sale(String product,int quantity){
        Product p = search(product);
        if(p.price<funds){
            System.out.println("You cant purchase "+ p.name + " Funds not available");   
        }
        else{
            funds-=p.price;
        }
        if(p.units<quantity){
            System.out.println("Sorry That much is not available in stock please reduce amount");
        }else{
            try {
                PreparedStatement ps = con.prepareStatement("update PRODUCT set units = ? where name = ?");
                ps.setInt(1, p.units-quantity);
                ps.setString(2, p.name);
                ps.execute();
                TotalEarning+=p.price;
            } catch (SQLException ex) {
                Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    void add_a_product(String customer_name,String name, int quantity){
        search(name);
        String s= "insert into cart(customername,name,quantity) VALUES(?,?,?)";
        try {
            PreparedStatement ps = con.prepareStatement(s);
            ps.setString(1, customer_name);
            ps.setString(2, name);
            ps.setInt(3, quantity);
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    void checkout(String Customer_name){
        try {
            String s = "SELECT * FROM CART WHERE customername = ?";
            PreparedStatement ps = con.prepareStatement(s);
            ps.setString(1, Customer_name);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                sale(rs.getString("name"),rs.getInt("quantity"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public static void main(String[] args) throws SQLException, IOException {
        // TODO code application logic here
        Scanner in = new Scanner(System.in);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try{
            Class.forName("com.mysql.jdbc.Driver");
            con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/db1","root","scorpion70");
        }
        catch(ClassNotFoundException e)
        {
            System.out.println(e);
        }
        int ch,flag=1,x;
        Product product = new Product(1,"oneplus",10000,10);
        String q="";
        Assignment a = new Assignment();
        a.init();
        while(true){
        System.out.println("Log IN as Admin(Press 1)");
        System.out.println("Log IN as Customer(Press 2)");
        System.out.println("Exit (press 3)");
        ch = in.nextInt();
        if(ch == 1){
            flag=1;
            while(flag==1){
            System.out.println("Insert Product/Category (Press 1)");
            System.out.println("Delete Product/Category (Press 2)");
            System.out.println("Search Product (Press 3)");
            System.out.println("Modify Product (Press 4)");
            System.out.println("Exit as Admin (Press 5)");
            ch = in.nextInt();
            switch(ch){
                case 1: 
                    System.out.println("Enter Path of Product/Category (For Eg. electronics>smartphone or"
                            + " electronics>smartphone>oneplus)");
         
                    q = br.readLine();
                
                    System.out.println("Enter Product Details:-");
                    System.out.println("Product id:- ");
                    product.id= in.nextInt();
                    System.out.println("Product name:- ");
                    product.name = br.readLine();
                    System.out.println("Product Price");
                    product.price = in.nextInt();
                    System.out.println("Product quantity");
                    product.units = in.nextInt();
                    a.insert(q, product);
                    break;
                case 2:
                    System.out.println("Enter Path of Product/Category to delete (For Eg. electronics>smartphone or"
                            + " electronics>smartphone>oneplus)");
                    q = br.readLine();
                    a.delete(q);
                    break;
                case 3:
                    System.out.println("Enter the name of the product to be searched");
                    q = br.readLine();
                    product = a.search(q);
                    System.out.println("Product Details:-");
                    System.out.println("ID: "+ product.id);
                    System.out.println("Name: "+ product.name);
                    System.out.println("Price: "+ product.price);
                    System.out.println("Units: "+ product.units);
                    break;
                case 4: 
                    a.update();
                    break;
                case 5: flag=0;
                    break;
            }  
            }
        }else if(ch == 2){
            flag=1;
            System.out.println("Please Enter your name");
                Customer_name = br.readLine();
            while(flag==1){
                
                
            System.out.println("Add Funds (Press 1)");
            System.out.println("Add Product to cart (Press 2)");
            System.out.println("Check-out Cart (Press 3)");
            System.out.println("Exit as Customer (Press 4)");
            ch = in.nextInt();
            switch(ch){
                case 1:
                    System.out.println("Enter the amount of funds to be added");
                    funds = in.nextInt();
                    break;
                case 2:
                    System.out.println("Enter the Product name and quantity");
                {
                    try {
                        q = br.readLine();
                    } catch (IOException ex) {
                        Logger.getLogger(Assignment.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                    x = in.nextInt();
                    a.add_a_product(Customer_name, q, x);
                    break;
                case 3:
                    a.checkout(Customer_name);
                    break;
                case 4:
                    flag=0;
                    break;
            }  
            }
        }
        else if(ch == 3){
            System.out.println("Total Earning is :- " + TotalEarning);
            break;
        }else{
            System.out.println("Please press 1,2 or 3");
        }
        }
        /*
        
        a.insert("electronics>smartphone", product);
        //a.delete("electronics>smartphone>oneplus");
        //product = a.search("oneplus");
        //System.out.println(product.id+" "+product.name);
        //a.update();
        a.add_a_product("RamLAL","oneplus",2);
        a.checkout("RamLAL");*/
    }
    
}
