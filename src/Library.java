import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;


public class Library {
	
	Connection con;
	
	public Library() {
		try { 
			Class.forName("org.sqlite.JDBC");
			con = DriverManager.getConnection("jdbc:sqlite:/Users/Jesse/Documents/ComputerScience/CS 364/Booknew.sqlite");
		}
		catch (Exception e) {
			 System.out.println("Connection failed"+e.getMessage());
		}
	}
	
	public boolean input() {
		boolean b = true;
		System.out.println("Which operation would you like to perform?");
		System.out.println("1. Display contents of any table.");
		System.out.println("2. Show the booknums and titles given an author's name.");
		System.out.println("3. Show the number of available copies of a book given the booknum.");
		System.out.println("4. Show a list of the current books on loan to a given customer.");
		System.out.println("5. Show a list of over due books for a given customer.");
		System.out.println("6. Load new customers from a test file given a file name.");
		System.out.println("7. Return a book given the copynum and the customer ID.");
		System.out.println("8. Exit.");
		System.out.print("Enter your choice: ");
		Scanner sc = new Scanner(System.in);
		int choice = sc.nextInt();
		System.out.println();
		switch (choice) {
			case 1: op1(sc);
			case 2: op2(sc);
			case 3: op3(sc);
			case 4: op4(sc);
			case 5: op5(sc);
			case 6: op6(sc);
			case 7: op7(sc);
				break;
			default: b = false;
				break;
		}
		sc.close();
		return b;
	}
	
	private void op7(Scanner sc) {
		// TODO Auto-generated method stub
		System.out.print("Customer ID: ");
		int cid = sc.nextInt();
		System.out.print("Copynum: ");
		int copynum = sc.nextInt();
		try {
			Statement s = con.createStatement();
			ResultSet r = s.executeQuery("Select L.copynum from Loan L, Customer C where L.cid = C.cid and cid = " + cid 
					+ " and ret is null and copynum = " + copynum );
			if (r.getInt(1) == copynum) {
				s.executeUpdate("Update Loan set ret = julianday('now')");
			}
			else {
				System.out.println("That copy has not been checked out to you.");
			}
		}
		catch (Exception e) {
			System.out.println("Query failed"+e.getMessage());
		}
	}

	private void op6(Scanner sc) {
		// TODO Auto-generated method stub
		System.out.print("Comma Delimmited File: ");
		String file = sc.next();
		System.out.println();
		try (BufferedReader br = new BufferedReader( new FileReader(file))) {
			String line = br.readLine();
			PreparedStatement p = con.prepareStatement("Insert into Customer values (?, ?, ?, ?)");
			while (line != null) {
				Scanner lineScan = new Scanner(line);
				lineScan.useDelimiter(",");
				p.setInt(1, lineScan.nextInt());
				p.setString(2, lineScan.next());
				p.setString(3, lineScan.next());
				p.setString(4, lineScan.next());
				lineScan.close();
				p.addBatch();
				line = br.readLine();
			}
			p.executeBatch();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in reading file. "+e.getMessage());
		}
		catch (Exception e) {
			System.out.println("Query failed"+e.getMessage());
		}
	}

	private void op5(Scanner sc) {
		// TODO Auto-generated method stub
		try {
			Statement s = con.createStatement();
			System.out.print("Customer ID: ");
			int cid = sc.nextInt();
			ResultSet r = s.executeQuery("Select L.copynum, title from Copy C, Loan L, Book B, Customer X where B.booknum = C.booknum and "
					+ "C.copynum = L.copynum and L.cid = X.cid and ret > julianday('now') and X.cid = " + cid);
			while (r.next()) {
				System.out.println(r. getString(1) + r.getString(2));
			}
			sc.close();
		}
		catch (Exception e) {
			System.out.println("Query failed"+e.getMessage());
		}
	}

	private void op4(Scanner sc) {
		// TODO Auto-generated method stub
		try {
			Statement s = con.createStatement();
			System.out.print("Customer ID: ");
			int cid = sc.nextInt();
			ResultSet r = s.executeQuery("Select L.copynum, title from Copy C, Loan L, Book B, Customer X where B.booknum = C.booknum and "
					+ "C.copynum = L.copynum and L.cid = X.cid and ret is null and X.cid = " + cid);
			while (r.next()) {
				System.out.println(r. getString(1) + r.getString(2));
			}
			sc.close();
		}
		catch (Exception e) {
			System.out.println("Query failed"+e.getMessage());
		}
	}

	private void op3(Scanner sc) {
		// TODO Auto-generated method stub
		try {
			Statement s = con.createStatement();
			System.out.print("Booknum: ");
			int booknum = sc.nextInt();
			ResultSet r = s.executeQuery("Select count(L.copynum from Copy C, Loan L, Book B where B.booknum = C.booknum and "
					+ "C.copynum = L.copynum and ret = out and B.booknum = " + booknum);
			while (r.next()) {
				System.out.println(r. getString(1));
			}
			sc.close();
		}
		catch (Exception e) {
			System.out.println("Query failed"+e.getMessage());
		}
	}

	private void op2(Scanner sc) {
		// TODO Auto-generated method stub
		try {
			Statement s = con.createStatement();
			System.out.print("Author's Name (first last): ");
			String first = sc.next();
			String last = sc.next();
			ResultSet r = s.executeQuery("Select B.booknum, title from Book B, Writes W, Author A where B.booknum = W.booknum and "
					+ "W.aid = A.aid and A.last = '" + last + "' and A.first = '" + first + "'" );
			while (r.next()) {
				System.out.println(r. getString(1) + r.getString(2));
			}
			sc.close();
		}
		catch (Exception e) {
			System.out.println("Query failed"+e.getMessage());
		}
	}

	private void op1 (Scanner sc) {
		// TODO Auto-generated method stub
		try {
			Statement s = con.createStatement();
			System.out.print("Table: ");
			String table = sc.next();
			ResultSet r = con.getMetaData().getColumns("", "", table, "");
			while	(r.next())	{	
				System.out.println(r.getString(4));	
			}
			System.out.println("Which of these columns would you like in the results?");
			System.out.println("(separate the columns with a comma and space):\n");
			String columns = sc.next();
			ResultSet r2 = s.executeQuery("Select " + columns + " from " + table);
			Scanner colScan = new Scanner(columns);
			colScan.useDelimiter(",");
			int numCol = 0;
			while(colScan.hasNext()) {
				sc.next();
				numCol++;
			}
			colScan.close();
			while (r2.next()) {
				for (int i = 1; i <= numCol; i++) {
					System.out.println(r.getString(i));
				}
			}
		}
		catch (Exception e) {
			System.out.println("Query failed"+e.getMessage());
		}
		
		
	}





	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Library l = new Library();
		boolean quit = false;
		System.out.println("Welcome to the library database!");
		while (!quit) {
			l.input();
		}
		System.out.println("Thanks for using the lLibrary database.");
		
		
		
	}

}
