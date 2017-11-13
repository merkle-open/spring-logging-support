<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Locale" %>

<!DOCTYPE html>
<html>
<head>
	<title>Commons. Logging. Live. Namics.</title>
	<meta charset="UTF-8" />
	<link rel="stylesheet" type="text/css" href="<c:url value="/css/styles.css" />">
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
</head>
<body>
	<h1>Demo. Commons. Logging. Live. Namics.</h1>
	<p>
	<% 
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.GERMAN);
		String formattedDate = dateFormat.format(date);
		%>
		<%= formattedDate %>
	</p>
	<a href="<c:url value="/nx-log/" />">Logging Demo</a>
</body>
</html>

