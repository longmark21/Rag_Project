import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseTest {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("数据库连通性测试");
        System.out.println("========================================");
        
        // 测试 MySQL
        testMySQL();
        
        // 测试 PostgreSQL
        testPostgreSQL();
        
        // 测试 Redis (使用 Socket 连接)
        testRedis();
        
        System.out.println("\n========================================");
        System.out.println("测试完成");
        System.out.println("========================================");
    }
    
    private static void testMySQL() {
        String url = "jdbc:mysql://192.168.31.29:3306/mysql?useSSL=false&serverTimezone=Asia/Shanghai";
        String user = "root";
        String password = "abc";
        
        System.out.println("\n1. 测试 MySQL 连接 (192.168.31.29:3306)...");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("   ✓ MySQL 连接成功！");
            System.out.println("   数据库版本：" + conn.getMetaData().getDatabaseProductVersion());
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("   ✗ MySQL 驱动未找到：" + e.getMessage());
        } catch (SQLException e) {
            System.out.println("   ✗ MySQL 连接失败：" + e.getMessage());
        }
    }
    
    private static void testPostgreSQL() {
        String url = "jdbc:postgresql://192.168.31.29:5433/postgres";
        String user = "postgres";
        String password = "abc";
        
        System.out.println("\n2. 测试 PostgreSQL 连接 (192.168.31.29:5433)...");
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("   ✓ PostgreSQL 连接成功！");
            System.out.println("   数据库版本：" + conn.getMetaData().getDatabaseProductVersion());
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("   ✗ PostgreSQL 驱动未找到：" + e.getMessage());
        } catch (SQLException e) {
            System.out.println("   ✗ PostgreSQL 连接失败：" + e.getMessage());
        }
    }
    
    private static void testRedis() {
        String host = "192.168.31.29";
        int port = 6379;
        
        System.out.println("\n3. 测试 Redis 连接 (192.168.31.29:6379)...");
        try {
            java.net.Socket socket = new java.net.Socket(host, port);
            System.out.println("   ✓ Redis TCP 连接成功！");
            socket.close();
        } catch (Exception e) {
            System.out.println("   ✗ Redis 连接失败：" + e.getMessage());
        }
    }
}
