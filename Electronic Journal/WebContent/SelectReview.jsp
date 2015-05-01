<%@page import="database.DBConnection"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=windows-1252">

<title>Team8 - E-Journal Site</title>
<link rel="stylesheet" type="text/css" href="major.css">
</head>
<body>
	<%@ page import="java.util.Date"%>
	<%@ page import="java.util.List"%>
	<%@ page import="review.*"%>
	<%@ page import="database.*"%>
	<div id="banner">
		<h1>International Journal of Software Engineering</h1>
	</div>
	<p><%= new Date() %></p>
	<div id="menubar">
		<li><a
			href="http://stucat.dcs.shef.ac.uk:8080/stucat008/index.jsp">
				Home</a></li>
		<li><a
			href="http://stucat.dcs.shef.ac.uk:8080/stucat008/review/reviewform.jsp">
				Submit Review Form</a></li>
		<li><a
			href="http://stucat.dcs.shef.ac.uk:8080/stucat008/changepassword.jsp">
				Change Password </a></li>
	</div>
	<div id="maincontent">
		<div id="rightdiv"></div>
		<!-- end right div -->
		<id="leftdiv">
		<div id="select">
			<br> <br> Please select the article you want to review
			<form
				action="http://localhost:8080/Electronic%20Journal/SelectReview.html"
				method=post>
				<table border="1" style="width: 100%">
				<%!  private List<Article> articles = null; %>
					<%  
				 		articles = SelectReview.getArticlesForReview(3); %>
				 		
					<%
						for (Article article : articles) {
					%>
					<tr>
						<td><input type="radio" name="article"
							value=<%=article.getArticleIDString()%>>Select</td>
						<td><%=
								article.getTitle()
							%></td>
						<td><input type="button"
							onclick="readAbstract(<%=article.getArticleIDString()%>)"
							value="Read Abstract"></td>
						<p hidden id=<%=article.getArticleIDString()%>><%=article.getArticleAbstract()%></p>
					</tr>

					<%
						}
					%>
				</table>
				<input type="submit" value="Choose">
			</form>
		</div>
		<p></p>
		<h3>Guideline for Reviewers</h3>
		Follow these guidelines while creating and submitting your review
		form:
		<p></p>
		<p>
			Download template <a
				href="http://stucat.dcs.shef.ac.uk:8080/stucat008/reviewtemplate.docx">Here</a>.
		</p>
		</id>
	</div>
	<!--End of left div -->

	<!-- End of Main content -->


	<!-- Footer Section -->
	<hr>
	<div id="footer">� Team8 2015</div>
</body>
</html>
<script type="text/javascript">
function readAbstract(p1) {
	var text = document.getElementById(p1);
	alert("article abstract : \n\n" + text.innerHTML);
}

</script>