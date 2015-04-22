import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import com.mysql.jdbc.Driver;
import org.apache.commons.fileupload.*;
import org.apache.commons.io.output.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;
import java.util.*;


public class SignUpServlet extends HttpServlet {

	private String email = "", password = "", cpassword = "", title = "", fname = "",
			lname = "", qualification = "", organisation = "",
			specialisation = "",message="";
	private String curDir ="";
	PrintWriter out;
	//Article credentials
	private String articleTitle="" , articleAbstract="",coauthors="",keywords="",filepath="uploads/";
	private File file;
	InputStream inps = null;
	private int maxFileSize = 10240000;
	private int authorid=0;
	private int role = 1, count = 0;
	private Connection dbCon = null; // connection to a database
	private String dbServer = "jdbc:mysql://stusql.dcs.shef.ac.uk/";
	private String dbname = "team158";
	private String user = "team158";
	private String myPassword = "9a5b309d";

	public SignUpServlet() {
		super();
		// Load database driver
		try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				System.err.println("fail to load driver.");
			}

	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		//Prepare HTML page for output
		res.setContentType("text/html");
		out = res.getWriter();
		ServletContext context = req.getServletContext();
		filepath = context.getInitParameter("filepath");
		curDir=req.getContextPath();
		message="<html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"major.css\"></head><body>";
		out.println(message);message="";
		
		//Article submission parameters
		articleTitle=req.getParameter("articletitle");
		articleAbstract = req.getParameter("articleabstract");
		coauthors = req.getParameter("coauthors");
		keywords = req.getParameter("keywords");
				
		//Author Registration Parameters for servlet 3.0
		email = req.getParameter("email");
		password = req.getParameter("password");
		cpassword = req.getParameter("cpassword");
		title = req.getParameter("title");
		lname = req.getParameter("lname");
		fname = req.getParameter("fname");
		organisation = req.getParameter("organisation");
		
		//Generate password for Lead Author
		String uuid = UUID.randomUUID().toString();
		message+="<span class=\"success\">";
		message += "<br/>";
		uuid= uuid.substring(24, 36);
		password=uuid;
		// verify content type
		String requestContentType = req.getContentType();
		if((requestContentType.indexOf("multipart/form-data")>=0)){
			DiskFileItemFactory dFactory = new DiskFileItemFactory();
			dFactory.setSizeThreshold(maxFileSize);
			
			
			//Now create a new file upload handler
			ServletFileUpload fUpload = new ServletFileUpload(dFactory);
			fUpload.setSizeMax(maxFileSize);
			String fieldName="",fieldValue;
			try{
				//Get the file items from the request
				List<FileItem> fileItems = fUpload.parseRequest(req);
				//Now process the uploaded file items
				Iterator<FileItem> i = fileItems.iterator();
				while(i.hasNext()){
					FileItem fItem = (FileItem)i.next();
					if(!fItem.isFormField()){
						//Get the uploaded file parameters and process them
					message += "<p>Trying to upload submited Article...</p><hr/>";
					String fileName = fItem.getName();
					
					//Stream will be saved to database as backup for file save.
					inps = fItem.getInputStream();
					if(inps!=null){
						message += "Got the file:" + fItem.getName()+"<br/>";
					}
					else{
						message += "Got NO file: Name=" + fItem.getName();
					}
					boolean isInMemory = fItem.isInMemory();
					long byteSize = fItem.getSize();
					//Write the article to the articles folder
					if(fileName.lastIndexOf("\\")>=0){
						file = new File(filepath + fileName.substring(fileName.lastIndexOf("\\" )));
					}
					else{
						file = new File(filepath + fileName.substring(fileName.lastIndexOf("\\") + 1));
					}
					//Saves to file
					fItem.write(file);
					//message+= "File uploaded to:" + file.getAbsolutePath() +"<br/>";
					filepath = file.getPath();
						
					}
					else{
						// Process the other form fields submitted
						fieldName=fItem.getFieldName();
						fieldValue = fItem.getString();
						//message += fieldName + ": " + fieldValue + "<br/>";
						switch(fieldName){
						case "articletitle":
							articleTitle=fieldValue; break;
						case "articleabstract":
							articleAbstract = fieldValue; break;
						case "coauthors":
							coauthors=fieldValue; break;
						case "keywords":
							keywords = fieldValue; break;
						case "email":
							email=fieldValue; break;
						case "password":
							password = fieldValue; break;
						case "cpassword":
							cpassword=fieldValue; break;
						case "fname":
							fname = fieldValue; break;
						case "title":
							title=fieldValue; break;
						case "lname":
							lname = fieldValue; break;
						default:
							//message+= "<p>Unknown field added</p>";
						}
					}
				}
				
			}
			catch(Exception ex){
				message= ex.getMessage();
				out.println("<span class=\"error\">Fileupload error: </span>" + message + ":"+ curDir);
				return;
			}
		}
		
		
				
		
		//Start session management here
		HttpSession session = req.getSession(true);
		

		if (email.trim().compareTo("") == 0) {
			message="Email address cannot be empty.</body></html>";
			out.println(message);
			return;
		}
		/*
		if (password.compareTo(cpassword) != 0) {
			message="Password and Confirmed password should be the same.</body></html>";
			out.println(message);
			return;
		}
		if (password.trim().compareTo("") == 0) {
			message="Password cannot be empty.</body></html>";
			out.println(message);
			return;
		}
		*/
		if (fname.trim().compareTo("") == 0) {
			message="First name cannot be empty.</body></html>";
			out.println(message);
			return;
		}
		
		
		authorid = this.getAuthorId();
		PreparedStatement pstmt = null;
		PreparedStatement pstmtArticle = null;

		try {
			// Get connection to team database
			dbCon = DriverManager.getConnection(dbServer + dbname, user,
					myPassword);
			int count=0;
			
			//Register new author only if not previously registered.
			if(authorid < 0){
				pstmt = dbCon
					.prepareStatement("INSERT INTO author VALUES (null, ?, ?,?,?,?,?,?,?)");
				pstmt.setString(1, email);
				pstmt.setString(2, password);
				pstmt.setString(3, title);
				pstmt.setString(4, fname);
				pstmt.setString(5, lname);
				pstmt.setString(6, qualification);
				pstmt.setString(7, organisation);
				pstmt.setString(8, specialisation);
				count = pstmt.executeUpdate();
			}

			if(count == 0){
				authorid = this.getAuthorId();
				message="Hello " + fname
				+ ", You have succesfully registered as an author<br/>";
				session.setAttribute("username",email);
				session.setAttribute("password",password);
				session.setAttribute("role","author");
				out.println(message);

			}
			//Handling article upload
			
			//Retrieve file here, do upload and set corrected file name
			
			pstmtArticle = dbCon.prepareStatement(
  				"INSERT INTO article(articleID,authorID,title,Other_authors,abstract,keywords,article_file,authoremail) VALUES (null, ?, ?,?,?,?,?,?)");
			pstmtArticle.setInt(1,authorid);
			pstmtArticle.setString(2,articleTitle);
			pstmtArticle.setString(3,coauthors);
			pstmtArticle.setString(4,articleAbstract);
			pstmtArticle.setString(5,keywords);
			pstmtArticle.setString(6,filepath);
			pstmtArticle.setString(7, email);
			//pstmtArticle.setBlob(7,inps);
			count = 0;
			count=pstmtArticle.executeUpdate();
			if(count > 0){
				message+="Article submission was successful</br>";
				session.setAttribute("article",articleTitle);
				message += "Username: " + email+"<br/>";
				message += "Password: " + uuid+"<br/></span>";
				//Set email session variables or send email from here
			}

		} catch (Exception ex) {

			ex.printStackTrace();
			out.println("<span class=\"error\">Article upload or Author Registration Error: </span>" + ex.getMessage());
			return;
		}

		finally {
			if (dbCon != null)
				try {
					dbCon.close();
				} catch (SQLException ex) {
				}
		}
		
		message+="</body></html>";
		out.println(message);
	}
	
	
	
	//Returns articles in the database that requires review
	public List<String> getArticles(){
		String articleslist="<table>";
		List<String> articles = new ArrayList<String>();
		try{
			// Get connection to team database
			dbCon = DriverManager.getConnection(dbServer + dbname, user,
								myPassword);
			Statement st = dbCon.createStatement();
			ResultSet result = st.executeQuery("Select * FROM article");
			while(result.next()){
				articleslist+= "<tr>";
				articles.add(result.getString("title"));
				articleslist+= "<td>"+result.getString("title")+"</td>";
				articles.add(result.getString("Other_authors"));
				articles.add(result.getString("abstract"));
				articleslist+= "<td>"+result.getString("abstract")+"</td>";
				articles.add(result.getString("keywords"));
				articleslist+= "<td>"+result.getString("keywords")+"</td>";
				articles.add(result.getString("article_file"));
				 articleslist+="<td><a href=\""+result.getString("article_file")
		          + "\">" + "</a></td></tr>";
			}
			
		}
		catch (Exception ex) {

			ex.printStackTrace();
			//out.println("<span class=\"error\">Article upload or Author Registration Error: </span>" + ex.getMessage());
			return articles;
		}

		finally {
			if (dbCon != null)
				try {
					dbCon.close();
				} catch (SQLException ex) {
				}
		}
		
		
		return articles;
		
	}
	
	public int getAuthorId(){
		int authorid=0;
		if(email==null || email.equals(""))
			return authorid;
		try{
			// Get connection to team database
			dbCon = DriverManager.getConnection(dbServer + dbname, user,
								myPassword);
			Statement st = dbCon.createStatement();
			ResultSet result = st.executeQuery("Select * FROM author WHERE username='"+ email + "'");
			while(result.next()){
				
				authorid=result.getInt("authorID");
				
			}
			
		}
		catch (Exception ex) {

			ex.printStackTrace();
			out.print("Error while getting user id:" + ex.getMessage());
			authorid=-1;
			return authorid;
		}

		finally {
			
		}
		
		
		return authorid;
		
	}
}