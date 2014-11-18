import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Scanner;


public class Library {
	
	Connection con;
	Calendar now;
	String nowS;
	
	public Library() {
		now = Calendar.getInstance();
		nowS = now.get(Calendar.YEAR) + "-" + now.get(Calendar.MONTH) + "-" + now.get(Calendar.DATE);
		try { 
			Class.forName("org.sqlite.JDBC");
			con = DriverManager.getConnection("jdbc:sqlite:/Users/Jon/Desktop/Library");
		}
		catch (Exception e) {
			 System.out.println("Connection failed"+e.getMessage());
		}
	}
	
	public boolean input(Scanner sc) {
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
		int choice = sc.nextInt();
		System.out.println();
		switch (choice) {
			case 1: op1(sc);
				break;
			case 2: op2(sc);
				break;
			case 3: op3(sc);
				break;
			case 4: op4(sc);
				break;
			case 5: op5(sc);
				break;
			case 6: op6(sc);
				break;
			case 7: op7(sc);
				break;
			default: b = false;
				break;
		}
		return b;
	}
	
	private void op7(Scanner sc) {
		// TODO Auto-generated method stub
		System.out.print("Customer ID: ");
		int cid = sc.nextInt();
		System.out.print("Copynum: ");
		int copynum = sc.nextInt();
		ResultSet r;
		String exp = "Select L.copynum from Loan L, Customer C where L.cid = C.cid and C.cid = '" + cid 
				+ "' and copynum = '" + copynum + "'";
		try {
			Statement s = con.createStatement();
			r = s.executeQuery(exp);
			if(r.next()) {
				s.executeUpdate("Update Loan set ret = '" + nowS + "' where cid = '" + cid + "' and copynum = '" + copynum + "'");
				System.out.println("You have returned that book.");
			}
			else
				System.out.println("That copy has not been checked out to you.");
		}
		catch (Exception e) { 
			System.out.println("Query failed "+e.getMessage());
		}
		System.out.println();
	}

	private void op6(Scanner sc) {
		// TODO Auto-generated method stub
		System.out.print("Enter a File with a | seperating the attributes: ");
		String file = sc.next();
		System.out.println();
		boolean fail = false;
		try (BufferedReader br = new BufferedReader( new FileReader(file))) {
			String line = br.readLine();
			PreparedStatement p = con.prepareStatement("Insert into Customer values (?, ?, ?, ?)");
			while (line != null) {
				String[] arr = line.split("\\|");
				p.setString(1, arr[0]);
				p.setString(2, arr[1]);
				p.setString(3, arr[2]);
				p.setString(4, arr[3]);
				p.addBatch();
				line = br.readLine();
			}
			p.executeBatch();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in reading file. "+e.getMessage());
			fail = true;
		}
		catch (Exception e) {
			System.out.println("Query failed. "+e.getMessage());
			fail = true;
		}
		if(!fail)
			System.out.println("Successful data entry.");
	}

	private void op5(Scanner sc) {
		// TODO Auto-generated method stub
		try {
			Statement s = con.createStatement();
			System.out.print("Customer ID: ");
			int cid = sc.nextInt();
			ResultSet r = s.executeQuery("Select L.copynum, title from Copy C, Loan L, Book B, Customer X where B.booknum = C.booknum and "
					+ "C.copynum = L.copynum and L.cid = X.cid and ret is null and due < '" + nowS + "' and X.cid = '" + cid + "'");
			if(r.next())
				System.out.println(r.getString(1) + " " + r.getString(2));
			else
				System.out.println("No books are overdue for this customer.");
			while (r.next()) {
				System.out.println(r.getString(1) + " " + r.getString(2));
			}
		}
		catch (Exception e) {
			System.out.println("Query failed"+e.getMessage());
		}
		System.out.println();
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
				System.out.println(r. getString(1) + " " + r.getString(2));
			}
		}
		catch (Exception e) {
			System.out.println("Query failed"+e.getMessage());
		}
		System.out.println();
	}

	private void op3(Scanner sc) {
		// TODO Auto-generated method stub
		// TODO get date and compare it to ret
		try {
			Statement s = con.createStatement();
			System.out.print("Booknum: ");
			
			int booknum = sc.nextInt();
			ResultSet r = s.executeQuery("Select count(L.copynum) from Copy C, Loan L, Book B where B.booknum = C.booknum and "
					+ "C.copynum = L.copynum and ret < '" + nowS + "' and B.booknum = " + booknum);
			while (r.next()) {
				System.out.println(r. getString(1));
			}
		}
		catch (Exception e) {
			System.out.println("Query failed"+e.getMessage());
		}
		System.out.println();
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
				System.out.println(r. getString(1) + " " +  r.getString(2));
			}
		}
		catch (Exception e) {
			System.out.println("Query failed"+e.getMessage());
		}
		System.out.println();
	}

	private void op1 (Scanner sc) {
		// TODO Auto-generated method stub
		try {
			Statement s = con.createStatement();
			System.out.print("Table: ");
			String table = "";
			table = sc.next();
			sc.nextLine();
			System.out.print("Columns (separate the columns with a comma and space): ");
			String columns = "";
			columns = sc.nextLine();
			ResultSet r2 = s.executeQuery("Select " + columns + " from " + table);
			String[] columnArr = columns.split(", ");
			int numCol = columnArr.length;
			while (r2.next()) {
				for (int i = 1; i <= numCol; i++) {
					System.out.print(r2.getString(i));
					if (i < numCol)
						System.out.print(", ");
				}
				System.out.println();
			}
		}
		catch (Exception e) {
			System.out.println("Query failed"+e.getMessage());
		}
		System.out.println();
	}
	
	public static void main(String[] args) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		Scanner sc = new Scanner(System.in);
		Library l = new Library();
		boolean quit = false;
		System.out.println("Welcome to the library database!");
		while (!quit) {
			quit = !(l.input(sc));
		}
		sc.close();
		System.out.println("Thanks for using the Library database.");
	}

}