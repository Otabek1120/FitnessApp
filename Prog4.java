
/**
 * Authors: 
 *      Ali Elbekov
 *      Aman Bhaia
 *      Eduardo Esau Ibarra
 *      Otabek Abduraimov
 *          
 * Class: CSC460 - Database Design
 * Instrcutor: Dr. McCann
 * Program: Prog4.java
 * 
 * Description:     This program lets user interact with a 
 *                  database for a fitness center. 
 *                  It allows users to:
 *                          insert a record
 *                          delete a record
 *                          update a record
 *                          run a few queries
 */

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.Scanner;
import java.util.TimeZone;

/*+----------------------------------------------------------------------
 ||
 ||  Class Prog4 
 ||
 ||        Purpose:  Java 16 program that embeddes SQL within a Java
 ||                  program.
 ||   Inherist From: None
 ||         
 ||      Interfaces: None 
 ||
 |+-----------------------------------------------------------------------
 ||
 ||      Constants:  oracleURL: "jdbc:oracle:thin:@aloe.cs.arizona.edu:15
 ||
 |+-----------------------------------------------------------------------
 ||
 ||   Constructors:  Just the default constructor; no arguments.
 ||
 ||  Class Methods:  None.
 ||
 ||  Inst. Methods:  getNextAction(Scanner user)
 ||                  insertMember(dbconn);
 ||                  deleteMember(dbconn);
 ||                  insertCourse(stmt);
 ||                  deleteCourse(stmt);
 ||                  manageCoursePackage(stmt);
 ||                  getMembersNegBalance(stmt);
 ||                  getMemberScheduleNov(stmt, user);
 ||                  getTrainersScheduleDec(stmt, user);
 ||                  customQuery(stmt);
 ++-----------------------------------------------------------------------*/
public class Prog4 {
    private static final String postgresURL = "jdbc:postgresql://localhost:5432/eddie";
    private static final String oracleURL = // Magic lectura -> aloe access spell
            "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";

    public static void main(String[] args) {
        boolean usePostgresURL = false;

        for (String arg : args) {
            usePostgresURL = arg.contains("postgres");
            if (usePostgresURL) {
                break;
            }
        }

        String username = System.getenv("db_username");
        String password = System.getenv("db_password");

        if (!usePostgresURL && args.length == 2) {
            username = args[0];
            password = args[1];
        } else if (!usePostgresURL && username.length() < 1) {
            System.out.println("\nUsage:  java Main <username> <password>\n"
                    + "    where <username> is your Oracle DBMS"
                    + " username,\n    and <password> is your Oracle"
                    + " password (not your system password).\n");
            System.exit(-1);
        }

        // make and return a database connection to the user's
        // Oracle database
        Connection dbconn = null;
        try {
            dbconn = DriverManager.getConnection(usePostgresURL ? postgresURL : oracleURL, username, password);
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                    + "Could not open JDBC connection.");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }

        Scanner user = new Scanner(System.in);

        boolean exit = false;
        try {
            while (!exit) {
                Statement stmt = dbconn.createStatement();
                int choice = getNextAction(user);

                switch (choice) {
                    case 1:
                        insertMember(dbconn);
                        break;
                    case 2:
                        deleteMember(dbconn);
                        break;
                    case 3:
                        insertCourse(stmt);
                        break;
                    case 4:
                        deleteCourse(stmt);
                        break;
                    case 5:
                        manageCoursePackage(stmt);
                        break;
                    case 6:
                        getMembersNegBalance(stmt);
                        break;
                    case 7:
                        getMemberScheduleNov(stmt, user);
                        break;
                    case 8:
                        getTrainersScheduleDec(stmt, user);
                        break;
                    case 9:
                        customQuery(stmt);
                        break;
                    case 10:
                        exit = true;
                        break;

                }
                stmt.close();

            }

            // Shut down the connection to the DBMS.
            user.close();
            dbconn.close();

        } catch (SQLException e) {

            System.err.println("*** SQLException:  "
                    + "Could not fetch query results.");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);

        }
    }

    /**
     * @Method: getNextAction(Scanner user)
     * @Description: This method returns the user's choice of next action.
     */
    private static int getNextAction(Scanner user) {

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   1.  Insert a new member                                    ║");
        System.out.println("║   2.  Delete a member                                        ║");
        System.out.println("║   3.  Insert a new course                                    ║");
        System.out.println("║   4.  Delete a course                                        ║");
        System.out.println("║   5.  Manage course packages                                 ║");
        System.out.println("║   6.  Get all Members with Negative Balance                  ║");
        System.out.println("║   7.  Get a Member's schedule for November                   ║");
        System.out.println("║   8.  Get all Trainers' schedule for December                ║");
        System.out.println("║   9.  Optional Query                                         ║");
        System.out.println("║   10. Quit the program.                                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.print("CHOOSE ONE OF THE OPTIONS ABOVE TO CONTINUE:\t\t");
        String input = user.nextLine();
        return Integer.parseInt(input);
    }

    /**
     * @Method: insertMember(Connection dbconn)
     * @Description: Adds a new member to the database
     *               Throws an exception if the query is invalid
     */
    private static void insertMember(Connection dbconn) throws SQLException {
        Scanner sc = new Scanner(System.in);

        // Prompt for member information
        System.out.println("\nINSERTING A NEW MEMBER...");
        System.out.print("\nEnter member name:\t");
        String memberName = sc.nextLine();

        System.out.print("\nEnter member phone number:\t");
        String memberPhone = sc.nextLine();

        // Fetch the highest existing member ID and increment it
        int memberId = getNextMemberId(dbconn);

        // Insert the member into the database
        String insertSql = "INSERT INTO Member (id, name, phone, membershipLevel, totalSpending) VALUES (?, ?, ?, 'Regular', 0.0)";
        PreparedStatement pstmt = dbconn.prepareStatement(insertSql);

        pstmt.setInt(1, memberId);
        pstmt.setString(2, memberName);
        pstmt.setString(3, memberPhone);

        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Member added successfully.");
            // printAllMembers(dbconn);
            displayPackagesWithCourses(dbconn, memberId);
            // Display available packages and handle package selection
        } else {
            System.out.println("Error: Member could not be added.");
        }

        pstmt.close();
    }

    /**
     * Displays available packages with courses, allowing the user to select and
     * link a package to a member.
     *
     * @param dbconn   The database connection.
     * @param memberId The ID of the member.
     * @throws SQLException If a database access error occurs.
     */
    private static void displayPackagesWithCourses(Connection dbconn, int memberId) throws SQLException {
        String packageQuery = "SELECT name, price FROM Package ORDER BY name";
        Statement packageStmt = dbconn.createStatement();
        ResultSet packageRs = packageStmt.executeQuery(packageQuery);

        List<String> packageNames = new ArrayList<>();
        int index = 1;

        while (packageRs.next()) {
            String packageName = packageRs.getString("name");
            double packagePrice = packageRs.getDouble("price");

            if (areCoursesAvailable(dbconn, packageName)) {
                packageNames.add(packageName); // Store package names for later reference
                System.out.printf("%d: %s (Price: $%.2f)\n", index++, packageName, packagePrice);
                displayCoursesForPackage(dbconn, packageName);
            }
        }

        packageStmt.close();
        selectAndLinkPackage(dbconn, packageNames, memberId);
    }

    /**
     * Checks if there are available slots in courses for a given package.
     *
     * @param dbconn      The database connection.
     * @param packageName The name of the package.
     * @return True if courses are available; false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    private static boolean areCoursesAvailable(Connection dbconn, String packageName) throws SQLException {
        String courseQuery = "SELECT c.maxParticipants, c.currentParticipants " +
                "FROM Course c JOIN PackageCourse pc ON c.name = pc.courseName " +
                "WHERE pc.packageName = ?";
        PreparedStatement courseStmt = dbconn.prepareStatement(courseQuery);
        courseStmt.setString(1, packageName);
        ResultSet courseRs = courseStmt.executeQuery();

        while (courseRs.next()) {
            int maxParticipants = courseRs.getInt("maxParticipants");
            int currentParticipants = courseRs.getInt("currentParticipants");
            if (currentParticipants >= maxParticipants) {
                courseStmt.close();
                return false; // Course is full, do not show this package
            }
        }

        courseStmt.close();
        return true; // All courses in the package have available space
    }

    /**
     * Displays the courses included in a given package.
     *
     * @param dbconn      The database connection.
     * @param packageName The name of the package.
     * @throws SQLException If a database access error occurs.
     */
    private static void displayCoursesForPackage(Connection dbconn, String packageName) throws SQLException {
        String courseQuery = "SELECT c.name, c.currentParticipants, c.maxParticipants " +
                "FROM Course c JOIN PackageCourse pc ON c.name = pc.courseName " +
                "WHERE pc.packageName = ?";
        PreparedStatement courseStmt = dbconn.prepareStatement(courseQuery);
        courseStmt.setString(1, packageName);
        ResultSet courseRs = courseStmt.executeQuery();

        while (courseRs.next()) {
            String courseName = courseRs.getString("name");
            int currentParticipants = courseRs.getInt("currentParticipants");
            int maxParticipants = courseRs.getInt("maxParticipants");
            System.out.printf("    Course: %s (Available: %d/%d)\n", courseName, currentParticipants, maxParticipants);
        }
        courseStmt.close();
    }

    /**
     * Prints information about all members in the database.
     *
     * @param dbconn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void printAllMembers(Connection dbconn) throws SQLException {
        String query = "SELECT * FROM Member";
        Statement stmt = dbconn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        System.out.println("ID\tName\t\tPhone\t\tMembership Level\tTotal Spending");
        System.out.println("-------------------------------------------------------------------------");
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String phone = rs.getString("phone");
            String membershipLevel = rs.getString("membershipLevel");
            double totalSpending = rs.getDouble("totalSpending");

            System.out.printf("%d\t%s\t%s\t%s\t\t%.2f\n", id, name, phone, membershipLevel, totalSpending);
        }

        stmt.close();
    }

    /**
     * Allows the user to select a package and links it to the specified member.
     *
     * @param dbconn       The database connection.
     * @param packageNames The list of available package names.
     * @param memberId     The ID of the member.
     * @throws SQLException If a database access error occurs.
     */
    private static void selectAndLinkPackage(Connection dbconn, List<String> packageNames, int memberId)
            throws SQLException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Select a package number: ");
        int choice = sc.nextInt();

        if (choice < 1 || choice > packageNames.size()) {
            System.out.println("Invalid choice. Please try again.");
            return;
        }

        // Fetching the memberId of the most recently added member
        // Assuming you are calling this right after adding a member

        String selectedPackageName = packageNames.get(choice - 1);
        linkMemberToPackage(dbconn, memberId, selectedPackageName);
    }

    /**
     * Links a member to a selected package, updating the database accordingly.
     *
     * @param dbconn      The database connection.
     * @param memberId    The ID of the member.
     * @param packageName The name of the selected package.
     * @throws SQLException If a database access error occurs.
     */
    private static void linkMemberToPackage(Connection dbconn, int memberId, String packageName) throws SQLException {
        String insertSql = "INSERT INTO PackageMembers (packageName, memberId) VALUES (?, ?)";
        PreparedStatement pstmt = dbconn.prepareStatement(insertSql);

        pstmt.setString(1, packageName);
        pstmt.setInt(2, memberId);

        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Member successfully linked to package " + packageName + ".");
            printPackageMembers(dbconn);
            updateCourseParticipants(dbconn, packageName);
            addDueTransaction(dbconn, packageName, memberId);
            printAllTransactions(dbconn);

        } else {
            System.out.println("Error: Could not link member to package.");
        }

        pstmt.close();
    }

    /**
     * Updates the current participants count in courses associated with a given
     * package.
     *
     * @param dbconn      The database connection.
     * @param packageName The name of the package.
     * @throws SQLException If a database access error occurs.
     */
    private static void updateCourseParticipants(Connection dbconn, String packageName) throws SQLException {
        String courseQuery = "SELECT courseName FROM PackageCourse WHERE packageName = ?";
        PreparedStatement courseStmt = dbconn.prepareStatement(courseQuery);
        courseStmt.setString(1, packageName);
        ResultSet courseRs = courseStmt.executeQuery();

        while (courseRs.next()) {
            String courseName = courseRs.getString("courseName");
            String updateSql = "UPDATE Course SET currentParticipants = currentParticipants + 1 WHERE name = ?";
            PreparedStatement updateStmt = dbconn.prepareStatement(updateSql);
            updateStmt.setString(1, courseName);
            updateStmt.executeUpdate();
            updateStmt.close();
        }
        courseStmt.close();
    }

    /**
     * Adds a due transaction for a linked member and package.
     *
     * @param dbconn      The database connection.
     * @param packageName The name of the package.
     * @param memberId    The ID of the member.
     * @throws SQLException If a database access error occurs.
     */
    private static void addDueTransaction(Connection dbconn, String packageName, int memberId) throws SQLException {
        // Fetch the package price
        String priceQuery = "SELECT price FROM Package WHERE name = ?";
        PreparedStatement priceStmt = dbconn.prepareStatement(priceQuery);
        priceStmt.setString(1, packageName);
        ResultSet priceRs = priceStmt.executeQuery();

        if (!priceRs.next()) {
            System.out.println("Error: Package not found.");
            return;
        }
        double packagePrice = priceRs.getDouble("price");
        priceStmt.close();

        // Generate a unique transaction ID
        int transactionId = getNextTransactionId(dbconn);

        // Insert the transaction
        String insertSql = "INSERT INTO Transaction (id, memberID, amount, transactionDate, transactionStatus) VALUES (?, ?, ?, CURRENT_DATE, 'DUE')";
        PreparedStatement insertStmt = dbconn.prepareStatement(insertSql);

        insertStmt.setInt(1, transactionId);
        insertStmt.setInt(2, memberId);
        insertStmt.setDouble(3, packagePrice);

        int rowsAffected = insertStmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Due transaction added successfully for member ID " + memberId + ".");
        } else {
            System.out.println("Error: Could not add due transaction.");
        }

        insertStmt.close();
    }

    /**
     * Retrieves the next available transaction ID from the database.
     *
     * @param dbconn The database connection.
     * @return The next available transaction ID.
     * @throws SQLException If a database access error occurs.
     */
    private static int getNextTransactionId(Connection dbconn) throws SQLException {
        String query = "SELECT MAX(id) FROM Transaction";
        Statement stmt = dbconn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        int nextId = 1; // Start from 1 if no transactions exist
        if (rs.next()) {
            nextId = rs.getInt(1) + 1; // Increment the highest ID by 1
        }

        stmt.close();
        return nextId;
    }

    /**
     * Prints information about all transactions in the database.
     *
     * @param dbconn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void printAllTransactions(Connection dbconn) throws SQLException {
        String query = "SELECT * FROM Transaction";
        Statement stmt = dbconn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        System.out.println("ID\tMember ID\tAmount\t\tTransaction Date\tStatus\t\tType");
        System.out.println("------------------------------------------------------------------------------------");
        while (rs.next()) {
            int id = rs.getInt("id");
            int memberId = rs.getInt("memberID");
            double amount = rs.getDouble("amount");
            Date transactionDate = rs.getDate("transactionDate");
            String status = rs.getString("transactionStatus");
            String type = rs.getString("transactionType");

            System.out.printf("%d\t%d\t\t%.2f\t\t%s\t\t%s\t\t%s\n", id, memberId, amount, transactionDate, status,
                    type);
        }

        stmt.close();
    }

    /**
     * Prints information about package members in the database.
     *
     * @param dbconn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void printPackageMembers(Connection dbconn) throws SQLException {
        String query = "SELECT * FROM PackageMembers";
        Statement stmt = dbconn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        System.out.println("Package Name\tMember ID");
        System.out.println("-------------------------");
        while (rs.next()) {
            String packageName = rs.getString("packageName");
            int memberId = rs.getInt("memberId");
            System.out.printf("%s\t\t%d\n", packageName, memberId);
        }

        stmt.close();
    }

    /**
     * Retrieves the next available member ID from the database.
     *
     * @param dbconn The database connection.
     * @return The next available member ID.
     * @throws SQLException If a database access error occurs.
     */
    private static int getNextMemberId(Connection dbconn) throws SQLException {
        String query = "SELECT MAX(id) FROM Member";
        Statement stmt = dbconn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        int nextId = 1; // Start from 1 if no members exist
        if (rs.next()) {
            nextId = rs.getInt(1) + 1; // Increment the highest ID by 1
        }

        stmt.close();
        return nextId;
    }

    /**
     * Deletes a member from the database, handling unreturned equipment, unpaid
     * balances, and active course participation.
     *
     * @param dbconn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void deleteMember(Connection dbconn) throws SQLException {
        Scanner sc = new Scanner(System.in);
        printAllMembers(dbconn);
        System.out.print("Enter member ID to delete: ");
        int memberId = sc.nextInt();

        // Check for unreturned equipment and mark as lost
        handleUnreturnedEquipment(dbconn, memberId);

        // Check for unpaid balances
        if (hasUnpaidBalances(dbconn, memberId)) {
            System.out.println("Member has unpaid balances. Cannot delete.");
            printUnpaidBalances(dbconn, memberId);
            return;
        }

        // Check for active course participation and remove
        handleActiveCourseParticipation(dbconn, memberId);

        // Delete member
        String deleteSql = "DELETE FROM Member WHERE id = ?";
        PreparedStatement pstmt = dbconn.prepareStatement(deleteSql);
        pstmt.setInt(1, memberId);

        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Member deleted successfully.");
        } else {
            System.out.println("Error: Member could not be deleted.");
        }

        pstmt.close();
    }

    /**
     * Marks unreturned equipment as lost and updates the available quantity.
     *
     * @param dbconn   The database connection.
     * @param memberId The ID of the member.
     * @throws SQLException If a database access error occurs.
     */
    private static void handleUnreturnedEquipment(Connection dbconn, int memberId) throws SQLException {
        String query = "SELECT equipmentName FROM Borrow WHERE memberId = ? AND returnTime IS NULL";
        PreparedStatement pstmt = dbconn.prepareStatement(query);
        pstmt.setInt(1, memberId);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            String equipmentName = rs.getString("equipmentName");
            // Mark equipment as lost and update available quantity
            String updateSql = "UPDATE Equipment SET available = available - 1 WHERE name = ?";
            PreparedStatement updateStmt = dbconn.prepareStatement(updateSql);
            updateStmt.setString(1, equipmentName);
            updateStmt.executeUpdate();
            updateStmt.close();

            System.out.println("Equipment " + equipmentName + " marked as lost for member " + memberId);
        }

        pstmt.close();
    }

    /**
     * Checks if a member has unpaid balances.
     *
     * @param dbconn   The database connection.
     * @param memberId The ID of the member.
     * @return True if the member has unpaid balances; false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    private static boolean hasUnpaidBalances(Connection dbconn, int memberId) throws SQLException {
        String query = "SELECT COUNT(*) FROM Transaction WHERE memberID = ? AND transactionStatus = 'DUE'";
        PreparedStatement pstmt = dbconn.prepareStatement(query);
        pstmt.setInt(1, memberId);
        ResultSet rs = pstmt.executeQuery();

        boolean hasUnpaid = rs.next() && rs.getInt(1) > 0;
        pstmt.close();
        return hasUnpaid;
    }

    /**
     * Prints information about unpaid balances for a specific member.
     *
     * @param dbconn   The database connection.
     * @param memberId The ID of the member.
     * @throws SQLException If a database access error occurs.
     */
    private static void printUnpaidBalances(Connection dbconn, int memberId) throws SQLException {
        String query = "SELECT id, amount FROM Transaction WHERE memberID = ? AND transactionStatus = 'DUE'";
        PreparedStatement pstmt = dbconn.prepareStatement(query);
        pstmt.setInt(1, memberId);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("Unpaid Balances:");
        while (rs.next()) {
            int transactionId = rs.getInt("id");
            double amount = rs.getDouble("amount");
            System.out.printf("Transaction ID: %d, Amount Due: %.2f\n", transactionId, amount);
        }

        pstmt.close();
    }

    /**
     * Handles active course participation for a member, updating course participant
     * numbers and deleting package member records.
     *
     * @param dbconn   The database connection.
     * @param memberId The ID of the member.
     * @throws SQLException If a database access error occurs.
     */
    private static void handleActiveCourseParticipation(Connection dbconn, int memberId) throws SQLException {
        String query = "SELECT packageName FROM PackageMembers WHERE memberId = ?";
        PreparedStatement pstmt = dbconn.prepareStatement(query);
        pstmt.setInt(1, memberId);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            String packageName = rs.getString("packageName");
            // Update course participant numbers
            updateCourseParticipantsOnMemberDeletion(dbconn, packageName);
            // Delete package member record
            deletePackageMemberRecord(dbconn, packageName, memberId);
        }

        pstmt.close();
    }

    /**
     * Updates course participant numbers when a member is deleted.
     *
     * @param dbconn      The database connection.
     * @param packageName The name of the package.
     * @throws SQLException If a database access error occurs.
     */
    private static void updateCourseParticipantsOnMemberDeletion(Connection dbconn, String packageName)
            throws SQLException {
        String courseQuery = "SELECT courseName FROM PackageCourse WHERE packageName = ?";
        PreparedStatement courseStmt = dbconn.prepareStatement(courseQuery);
        courseStmt.setString(1, packageName);
        ResultSet courseRs = courseStmt.executeQuery();

        while (courseRs.next()) {
            String courseName = courseRs.getString("courseName");
            String updateSql = "UPDATE Course SET currentParticipants = currentParticipants - 1 WHERE name = ?";
            PreparedStatement updateStmt = dbconn.prepareStatement(updateSql);
            updateStmt.setString(1, courseName);
            updateStmt.executeUpdate();
            updateStmt.close();
        }

        courseStmt.close();
    }

    /**
     * Deletes package member records for a specific member and package.
     *
     * @param dbconn      The database connection.
     * @param packageName The name of the package.
     * @param memberId    The ID of the member.
     * @throws SQLException If a database access error occurs.
     */
    private static void deletePackageMemberRecord(Connection dbconn, String packageName, int memberId)
            throws SQLException {
        String deleteSql = "DELETE FROM PackageMembers WHERE packageName = ? AND memberId = ?";
        PreparedStatement pstmt = dbconn.prepareStatement(deleteSql);
        pstmt.setString(1, packageName);
        pstmt.setInt(2, memberId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    /**
     * Inserts a new course into the database.
     *
     * @param stmt The SQL statement.
     * @throws SQLException If a database access error occurs.
     */
    private static void insertCourse(Statement stmt) throws SQLException {
        Scanner sc = new Scanner(System.in);
        String query = buildQuery();

        System.out.print("\nAre you sure to add a new course? [y/n]:\t");
        String input = sc.next();
        if (input.equals("y") || input.equals("Y")) {
            ResultSet response = stmt.executeQuery(query);
            if (response != null) {
                System.out.println("New course added successfully!");
            }
        }

    }

    /**
     * Builds the SQL query for inserting a new course by
     * taking details from the user
     *
     * @return The constructed SQL query.
     */
    private static String buildQuery() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter course name:\t");
        String courseName = sc.nextLine();
        System.out.println();

        System.out.print("Enter trainer id:\t");
        int trainerID = sc.nextInt();
        System.out.println();

        System.out.print("Enter weekly class day (monday/tuesday...):\t");
        String weeklyClass = sc.next();
        System.out.println();

        System.out.print("Enter start date (MM/DD/YYYY):\t");
        String startDate = sc.next();
        System.out.println();

        System.out.print("Enter end date (MM/DD/YYYY):\t");
        String endDate = sc.next();
        System.out.println();

        System.out.print("Enter start time (0, 1200, or 1420):\t");
        String startTime = sc.next();
        System.out.println();

        System.out.print("Enter end time (0, 1200, or 1420):\t");
        String endTime = sc.next();
        System.out.println();

        int currParticipants = 0;

        System.out.print("Enter max participants:\t");
        int maxParticipants = sc.nextInt();
        System.out.println();

        String query = "insert into course values (" +
                "\'" + courseName + "\', " +
                trainerID + ", " +
                "\'" + weeklyClass + "\', " +
                "to_date('" + startDate + "\'," + "\'MM/DD/YYYY\'), " +
                "to_date('" + endDate + "\'," + "\'MM/DD/YYYY\'), " +
                startTime + ", " +
                endTime + ", " +
                currParticipants + ", " +
                maxParticipants + ")";
        System.out.println(query);

        return query;
    }

    /**
     * Deletes a course from the database and notifies
     * current enrolled members.
     *
     * @param stmt The SQL statement.
     * @throws SQLException If a database access error occurs.
     */
    private static void deleteCourse(Statement stmt) throws SQLException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a course name to delete (Yoga 001, Strength 002):\t");
        String course = sc.nextLine();
        Map<String, String> members = getMembers(course, stmt);
        System.out.println("Memeber who need to be notified:");
        System.out.println("══════════════════════════════════════════════════════");
        for (String member : members.keySet()) {
            System.out.println(member + ": " + members.get(member));
        }
        System.out.println("══════════════════════════════════════════════════════");

        System.out.print("Continue? [y/n]:\t");
        String input = sc.next();
        if (input.equals("y") || input.equals("Y")) {
            System.out.println("\nDeleting " + "\'" + course + "\'");
            deleteCourseRecord(stmt, course);
            System.out.println("\'" + course + "\'" + " deleted successfully!");
        }

    }

    /**
     * Retrieves members enrolled in a course for notification.
     *
     * @param course The course name.
     * @param stmt   The SQL statement.
     * @return A map of member names and their phone numbers.
     * @throws SQLException If a database access error occurs.
     */
    private static Map<String, String> getMembers(String course, Statement stmt) throws SQLException {
        Map<String, String> namePhoneMap = new HashMap<>();
        String query = "SELECT name, phone " +
                "FROM member " +
                "WHERE id IN (SELECT memberid " +
                "FROM PackageMembers " +
                "WHERE packageName IN (SELECT packagename " +
                "FROM packagecourse " +
                "WHERE coursename = \'" + course + "\'))";
        ResultSet result = stmt.executeQuery(query);
        if (result != null) {
            while (result.next()) {
                String name = result.getString("name");
                String phone = result.getString("phone");
                namePhoneMap.put(name, phone);
            }

        }
        return namePhoneMap;
    }

    /**
     * Deletes a course and related records from the database.
     *
     * @param stmt   The SQL statement.
     * @param course The course name.
     */
    private static void deleteCourseRecord(Statement stmt, String course) {
        String query1 = "DELETE FROM PackageCourse WHERE courseName = \'" + course + "\'";
        try {
            stmt.executeQuery(query1);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String query2 = "DELETE FROM Course WHERE name = \'" + course + "\'";
        try {
            stmt.executeQuery(query2);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * Manages the insertion, update, or deletion of a course package.
     *
     * @param stmt The SQL statement.
     * @throws SQLException If a database access error occurs.
     */
    private static void manageCoursePackage(Statement stmt) throws SQLException {
        Scanner sc = new Scanner(System.in);
        System.out.println(" OPERATION SELECTION: TYPE insert / update / delete");
        String operation = sc.nextLine();
        if (operation.equals("insert")) {
            System.out.println("Enter the name of the package course to add: ");
            String packageName = sc.nextLine();
            System.out.println("Enter course to include inside the package: ");
            String courseName = sc.nextLine();
            String add_query = "INSERT INTO packagecourse values (" +
                    "\'" + packageName + "\', " +
                    "\'" + courseName + "')";
            stmt.executeUpdate(add_query);
        }
        if (operation.equals("delete")) {
            System.out.println("What is the name of the package you want to delete?");
            String packageName = sc.nextLine();
            String delete_query = "DELETE FROM packagecourse WHERE packageName = \'" +
                    packageName + "\'";
            stmt.executeUpdate(delete_query);
        }
        if (operation.equals("update")) {
            System.out.println("What is the name of the package you want to update?");
            String packageName = sc.nextLine();
            System.out.println("What is the name of the course you want to update?");
            String oldCourse = sc.nextLine();
            System.out.println("What new course would you like in the course package?");
            String newCourse = sc.nextLine();
            String update_query = "UPDATE packagecourse SET coursename = \'" +
                    newCourse + "\' where packagename = \'" + packageName + "\' and courseName = '" + oldCourse + "'";
            stmt.executeUpdate(update_query);
        }
    }

    /**
     * Retrieves members with a negative balance and prints their names and phone
     * numbers.
     *
     * @param stmt The SQL statement.
     * @throws SQLException If a database access error occurs.
     */
    private static void getMembersNegBalance(Statement stmt) throws SQLException {

        String query = "SELECT name, phone " +
                "FROM member " +
                "WHERE id IN (SELECT memberId " +
                "FROM transaction " +
                "WHERE transactionStatus = 'DUE' and CURRENT_DATE >= transactionDate)";
        // System.out.println(query);
        ResultSet resultSet = stmt.executeQuery(query);
        if (resultSet != null) {
            System.out.println("THE RESULTS FOR [Members with Negative Balance]:");
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║ Name" + "\t\t║ " + "Phone number \t                        ║");
            System.out.println("║════════════════════════════════════════════════════════════║");

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String phone = resultSet.getString("phone");
                System.out.println("║ " + name + "\t║ " + phone);
            }
            System.out.println("╚═════════════════════════════════════════════════════════════╝");
            ;

        }
    }

    /**
     * Converts an integer representing time in military format to a string in
     * regular format.
     *
     * @param militaryTime The integer representing time in military format
     * @return A time string in regular format, or null if militaryTime is invalid.
     */
    private static String militaryTimeToRegularTime(int militaryTime) {
        if (militaryTime < 0) {
            militaryTime *= -1;
        }

        int minutes = militaryTime % 100; // maybe should be % 60;
        int hours = militaryTime / 100;

        if (minutes >= 60 || hours >= 24) {
            return null;
        }

        String suffix = hours >= 12 ? " PM" : " AM";

        if (hours >= 13) {
            hours -= 12;
        } else if (hours == 0) {
            hours = 12;
        }

        return hours + ":" + String.format("%02d", minutes) + suffix;
    }

    /**
     * Converts a string representing a day of the week to java's calender
     * representations.
     *
     * @param dayOfTheWeek String representing a day of the week from the database.
     * @return Integer representing a day of the week in java's calender.
     */
    private static int dayOfTheWeekIndex(String dayOfTheWeek) {
        return switch (dayOfTheWeek) {
            case "sunday" -> Calendar.SUNDAY;
            case "monday" -> Calendar.MONDAY;
            case "tuesday" -> Calendar.TUESDAY;
            case "wednesday" -> Calendar.WEDNESDAY;
            case "thursday" -> Calendar.THURSDAY;
            case "friday" -> Calendar.FRIDAY;
            case "saturday" -> Calendar.SATURDAY;
            default -> -1;
        };
    }

    /**
     * Converts a calender date to a string.
     *
     * @param cal Calender set to the date desired.
     * @return A string representing the date that the calender is set to.
     */
    private static String dateToString(Calendar cal) {
        String dayOfWeek = switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY -> "Sun";
            case Calendar.MONDAY -> "Mon";
            case Calendar.TUESDAY -> "Tue";
            case Calendar.WEDNESDAY -> "Wed";
            case Calendar.THURSDAY -> "Thu";
            case Calendar.FRIDAY -> "Fri";
            case Calendar.SATURDAY -> "Sat";
            default -> throw new IllegalStateException("Unexpected value: " + cal.get(Calendar.DAY_OF_WEEK));
        };

        String month = switch (cal.get(Calendar.MONTH)) {
            case Calendar.JANUARY -> "Jan";
            case Calendar.FEBRUARY -> "Feb";
            case Calendar.MARCH -> "Mar";
            case Calendar.APRIL -> "Apr";
            case Calendar.MAY -> "May";
            case Calendar.JUNE -> "Jun";
            case Calendar.JULY -> "Jul";
            case Calendar.AUGUST -> "Aug";
            case Calendar.SEPTEMBER -> "Sep";
            case Calendar.OCTOBER -> "Oct";
            case Calendar.NOVEMBER -> "Nov";
            case Calendar.DECEMBER -> "Dec";
            default -> throw new IllegalStateException("Unexpected value: " + cal.get(Calendar.DAY_OF_WEEK));
        };

        return String.format("%s %s %02d %d", dayOfWeek, month, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.YEAR));
    }

    /**
     * Query 2: Check and see a member’s class schedule for November.
     *
     * @param stmt The statement form the database connection.
     * @param user The scanner for user input
     */
    private static void getMemberScheduleNov(Statement stmt, Scanner user) throws SQLException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Phoenix"));
        System.out.print("Name of the member: ");
        String memberName = user.nextLine(); // Get the name of the member for the query.

        // Execute query
        ResultSet rs = stmt.executeQuery(
                "select Course.name, weeklyclasstime, starttime, endtime, startdate, enddate from course " +
                        "join packagecourse on course.name = packagecourse.coursename " +
                        "join packagemembers on packagecourse.packagename = packagemembers.packagename " +
                        "join member on packagemembers.memberid = member.id and member.name = '" + memberName + "'");

        // Iterate through the result set.
        while (rs.next()) {
            String courseName = rs.getString(1);
            int weeklyClassTime = dayOfTheWeekIndex(rs.getString(2));
            int startTime = rs.getInt(3);
            int endTime = rs.getInt(4);
            Date startDate = rs.getDate(5, cal);
            Date endDate = rs.getDate(6, cal);

            // Check if weeklyClassTime is invalid.
            if (weeklyClassTime == -1) {
                continue; // fixme: invalid data, maybe throw exception.
            }

            // Calculate the first and last november from the start and end date of the
            // course.
            Date novemberStart;
            Date novemberEnd;

            cal.setTime(startDate);
            if (cal.get(Calendar.MONTH) < Calendar.NOVEMBER) {
                cal.set(Calendar.MONTH, Calendar.NOVEMBER);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                novemberStart = cal.getTime();
            } else if (cal.get(Calendar.MONTH) == Calendar.NOVEMBER) {
                novemberStart = cal.getTime();
            } else {
                cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
                cal.set(Calendar.MONTH, Calendar.NOVEMBER);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                novemberStart = cal.getTime();
            }

            cal.setTime(endDate);
            if (cal.get(Calendar.MONTH) < Calendar.NOVEMBER) {
                cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
                cal.set(Calendar.MONTH, Calendar.NOVEMBER);
                cal.set(Calendar.DAY_OF_MONTH, 30);
                novemberEnd = cal.getTime();
            } else if (cal.get(Calendar.MONTH) == Calendar.NOVEMBER) {
                novemberEnd = cal.getTime();
            } else {
                cal.set(Calendar.MONTH, Calendar.NOVEMBER);
                cal.set(Calendar.DAY_OF_MONTH, 30);
                novemberEnd = cal.getTime();
            }

            System.out.println(courseName + ": ");
            java.util.Date currentDate = novemberStart;

            // Iterate through the dates between start and end date printing the dates that
            // the class from the database
            // meets. The loop skips over non-november dates.
            while (novemberEnd.after(currentDate) || novemberEnd.equals(currentDate)) {
                cal.setTime(currentDate);

                if (cal.get(Calendar.DAY_OF_WEEK) == weeklyClassTime) {
                    System.out.println("  " + dateToString(cal) + ": " + militaryTimeToRegularTime(startTime) + " - "
                            + militaryTimeToRegularTime(endTime));
                }

                cal.add(Calendar.DATE, 1);
                Date oldDate = currentDate;
                currentDate = cal.getTime();
                cal.setTime(currentDate);
                if (cal.get(Calendar.MONTH) != Calendar.NOVEMBER) {
                    cal.setTime(oldDate);
                    cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
                    cal.set(Calendar.MONTH, Calendar.NOVEMBER);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    currentDate = cal.getTime();
                }
            }
        }

        rs.close();
    }

    /**
     * Query 3: Check and see all trainers’ working hours for December.
     *
     * @param stmt The statement form the database connection.
     * @param user The scanner for user input
     */
    private static void getTrainersScheduleDec(Statement stmt, Scanner user) throws SQLException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Phoenix"));
        System.out.print("Name of the trainer: ");
        String trainerName = user.nextLine(); // Get the name of the member for the query.

        // Execute query
        ResultSet rs = stmt.executeQuery(
                "select Course.name, weeklyclasstime, starttime, endtime, startdate, enddate from course " +
                        "join trainer on trainerid = trainer.id and trainer.name = '" + trainerName + "'");

        // Iterate through the result set.
        while (rs.next()) {
            String courseName = rs.getString(1);
            int weeklyClassTime = dayOfTheWeekIndex(rs.getString(2));
            int startTime = rs.getInt(3);
            int endTime = rs.getInt(4);
            Date startDate = rs.getDate(5, cal);
            Date endDate = rs.getDate(6, cal);

            if (weeklyClassTime == -1) {
                continue; // fixme: invalid data, maybe throw exception.
            }

            // Calculate the first and last december from the start and end date of the
            // course.
            Date novemberStart;
            Date novemberEnd;

            cal.setTime(startDate);
            if (cal.get(Calendar.MONTH) < Calendar.DECEMBER) {
                cal.set(Calendar.MONTH, Calendar.DECEMBER);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                novemberStart = cal.getTime();
            } else if (cal.get(Calendar.MONTH) == Calendar.DECEMBER) {
                novemberStart = cal.getTime();
            } else {
                cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
                cal.set(Calendar.MONTH, Calendar.DECEMBER);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                novemberStart = cal.getTime();
            }

            cal.setTime(endDate);
            if (cal.get(Calendar.MONTH) < Calendar.DECEMBER) {
                cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
                cal.set(Calendar.MONTH, Calendar.DECEMBER);
                cal.set(Calendar.DAY_OF_MONTH, 30);
                novemberEnd = cal.getTime();
            } else if (cal.get(Calendar.MONTH) == Calendar.DECEMBER) {
                novemberEnd = cal.getTime();
            } else {
                cal.set(Calendar.MONTH, Calendar.DECEMBER);
                cal.set(Calendar.DAY_OF_MONTH, 30);
                novemberEnd = cal.getTime();
            }

            System.out.println(courseName + ": ");
            java.util.Date currentDate = novemberStart;

            // Iterate through the dates between start and end date printing the dates that
            // the class from the database
            // meets. The loop skips over non-november dates.
            while (novemberEnd.after(currentDate) || novemberEnd.equals(currentDate)) {
                cal.setTime(currentDate);

                if (cal.get(Calendar.DAY_OF_WEEK) == weeklyClassTime) {
                    System.out.println("  " + dateToString(cal) + ": " + militaryTimeToRegularTime(startTime) + " - "
                            + militaryTimeToRegularTime(endTime));
                }

                cal.add(Calendar.DATE, 1);
                Date oldDate = currentDate;
                currentDate = cal.getTime();
                cal.setTime(currentDate);
                if (cal.get(Calendar.MONTH) != Calendar.DECEMBER) {
                    cal.setTime(oldDate);
                    cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
                    cal.set(Calendar.MONTH, Calendar.DECEMBER);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    currentDate = cal.getTime();
                }
            }
        }

        rs.close();
    }

    /**
     * Query 4: Shows the names of all members that have borrowed an equipment
     * that is no longer available to be borrowed.
     *
     * @param dbconn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void customQuery(Statement stmt) throws SQLException {
        String query = "Select id, member.name, equipment.name, phone from MEMBER" +
                " JOIN BORROW on member.id = borrow.memberid " +
                " JOIN EQUIPMENT on borrow.equipmentname = equipment.name" +
                " WHERE equipment.available = 0 ";

        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            String id = rs.getString(1);
            String name = rs.getString(2);
            String phone = rs.getString(3);
            System.out.println(id + "\t" + name + "\t" + phone);
        }

    }

}
