<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Use cases</h2>
	<h4>Use case two: Index a database</h4>
	Index a database, all the tables, paying no attention to the names of the tables or the columns:<br><br>

	<jsp:include page="/WEB-INF/jsp/use-cases/index-context.jspf" />
	<br><br>
	
	2) Create the datasource: To index a database you have to have the database details. This bean will define where it is(i.e. the url) and how to access it(userid and password). Some other 
	details are also specified like the fail over and connection recovery. We won't concentrate too much on these details, you can accept the defaults. What yo do need to fill in is the userid and 
	the password that you use to connect to your database. For the driver class, you can use org.h2.Driver for the H2 database, 
	or com.ibm.db2.jcc.DB2Driver for DB2, or oracle.jdbc.driver.OracleDriver forOracle or org.postgresql.Driver for Postgres.If you have another database then you must add the 
	driver jar to the common libs directory in the server, and specify the class name of your driver, perhaps MySql for example. But we will assume that you are using a database which has direct driver 
	support in Ikube(DB2, Oracle, Postgres and H2). Copy the snippit below and paste it into your configuration file below the myIndex bean. Replacing the userid, the password, the url and the driver placeholders with your details 
	of course.  
	
	<textarea rows="15" cols="30">
		<bean id="myIndexDatasource"
			class="com.mchange.v2.c3p0.ComboPooledDataSource"
			destroy-method="close"
			property:user="your-user-id-here"
			property:password="your-password-here"
			property:driverClass="your-database-driver-class"
			property:jdbcUrl="your-database-url"
			property:initialPoolSize="${jdbc.minPoolSize}"
			property:maxPoolSize="${jdbc.maxPoolSize}"
			property:maxStatements="${jdbc.maxStatements}"
			property:checkoutTimeout="${jdbc.checkOutTimeout}"
			property:numHelperThreads="${jdbc.numHelperThreads}"
			property:breakAfterAcquireFailure="${jdbc.breakAfterAcquireFailure}"
			property:debugUnreturnedConnectionStackTraces="${jdbc.debugUnreturnedConnectionStackTraces}"
			property:testConnectionOnCheckin="${jdbc.testConnectionOnCheckin}"
			property:testConnectionOnCheckout="${jdbc.testConnectionOnCheckout}"
			property:unreturnedConnectionTimeout="${jdbc.unreturnedConnectionTimeout}" />
	</textarea>
	<br><br>
	
	3) Create the database configuration: We have definedthe datasource, now we will create the bean that will index the datasource. First the children of the myIndex bean, then a reference to the 
	data source bean. There is a reference from 'myIndexDatabase' to 'myIndexDatasource', see it? When the inxeding starts, then the database beanwill use the data source bean to get a connection to 
	the database, list all the tables and index them one at a time, every column in every table. Copy the snippitbelow and paste it into your configuration file below the 'myIndexDatasource' bean but above 
	the end beans tag. 
	<br><br>
	
	<textarea rows="15" cols="30">
		<util:list id="myIndexChildren">
			<ref local="myIndexDatabase" />
		</util:list>
		
		<bean
			id="myIndexDatabase"
			class="ikube.model.IndexableDataSource"
			property:name="myIndexDatabase"
			property:dataSource-ref="myIndexDatasource"
			property:excludedTablePatterns="SYS:$" />
	</textarea>
	<br><br>
	
	<jsp:include page="/WEB-INF/jsp/use-cases/add-config.jspf" />
	
</div>