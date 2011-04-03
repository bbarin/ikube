<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="2">
			<span class="top-content-header">configuration</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			<strong>Basics</strong><br>
			Ikube can index four types of data sources, database, internet, file system and email. Each one of these 
			needs to be configured with client specific properties, for example if you want to index your web site then 
			the url for your web site needs to be place in the client spring.properties file. If you would like to index your 
			database the the datasource spring.xml file needs to be configured to point to your database and the tables 
			and columns need to be defined as well. All configuration is done via Spring configuration files. There are 
			examples of these files that can be modified to suit client specific needs. 
		</td>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			Ikube will look for configuration files in the directory where the Jvm was started first. If no configuration files 
			are found in this directory then Ikube will use the files in the META-INF directory packaged in the jar. The files 
			can be extracted from the jar, modified to suit and placed outside the war where Ikube will find them. If Ikube 
			is to run in a cluster then either the files in the war WEB-INF need to be modified to suit the client so that the 
			war can be dropped into the cluster as is or farmed out as in the case of JBoss, or each server needs to have 
			configuration files placed at it's base.  
		</td>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			In the case of Tomcat for example, the startup directory is typically in the ${TOMCAT_INSTALL_DIRECTORY}/bin. 
			So the configuration files will need to go here for Ikube to find them. Once the configuration is done and working 
			as it should then the files can be put back in the war where it is more convenient and the files in the bin directory 
			can be removed.
		</td>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			Data sources are contained in an index context. Essentially an index context is a container for an index. It also 
			is the container for the data sources. In a simple index there would be one index context and one internet data 
			source. In the default configuration, spring-client.xml, there is an example of an internet data source configuration. 
			There is also an example in the spring-minimal.xml configuration. For each table, column, internet site and email 
			account you need to create an indexable in the Spring configuration. It is recommended that the default 
			configuration that is already present is used for this and just modified.  
		</td>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			
		</td>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			Please refer to the table below for a full account of the available parameters and their definitions:
		</td>
	</tr>
	
	<tr>
		<th colspan="2">IndexContext definition parameters</th>
	</tr>
	
	<tr>
		<th class="td-content">Parameter</th>
		<th class="td-content">Description</th>
	</tr>
	<tr>
		<td class="td-content"> indexName</td>
		<td class="td-content"> 
			This is the name of the index. The name appended to the index directory path will be the location of this particular 
			index. Also 	the index will be accessed via this name when using the web service. This name must be unique within the 
			configuration.
		</td>
	</tr>
	<tr>
		<td class="td-content"> maxAge</td>
		<td class="td-content"> 
			The maximum age the index can become before a new one is generated. An index is triggered by an index 
			passing the maximum age. This is defined in milli seconds. 
		</td>
	</tr>
	<tr>
		<td class="td-content"> throttle</td>
		<td class="td-content"> 
			This parameter defines the time each thread will sleep between index items. In the case of the web crawler, 
			 to avoid stressing the server, each thread will sleep between reading urls. In the case of a database index each 
			 thread will sleep between records for this period of time, defined in milli seconds. 
		</td>
	</tr>
	<tr>
		<td class="td-content"> mergeFactor</td>
		<td class="td-content"> 
			Refer to the <a href="http://lucene.apache.org/java/docs/index.html" target="_top">Lucene</a> documentation for information 
			on the merge factor. Essentially it is the number of segments that are kept in memory during the merge, which could be 
			when the index is optimized or committed.      
		</td>
	</tr>
	<tr>
		<td class="td-content"> bufferedDocs</td>
		<td class="td-content"> 
			Refer to the <a href="http://lucene.apache.org/java/docs/index.html" target="_top">Lucene</a> documentation for information 
			on the buffered documents. This is the number of documents that will be stored in memory before being committed to the 
			index during indexing.
		</td>
	</tr>
	<tr>
		<td class="td-content"> bufferSize</td>
		<td class="td-content"> 
			Refer to the <a href="http://lucene.apache.org/java/docs/index.html" target="_top">Lucene</a> documentation for information 
			on the buffer size. This is the maximum size of the memory that Lucene can use before it commits the documents. A 
			fail safe for out of memory errors.
		</td>
	</tr>
	<tr>
		<td class="td-content"> maxFieldLength</td>
		<td class="td-content"> 
			Refer to the <a href="http://lucene.apache.org/java/docs/index.html" target="_top">Lucene</a> documentation for information 
			on the maximum field length.
		</td>
	</tr>
	<tr>
		<td class="td-content">compoundFile</td>
		<td class="td-content"> 
			Whether Lucene should use a compound file or not. During the indexing there will be several files generated by Lucene. At the 
			end the index will be optimized and potentially all the files will be merged into one file. With large indexes this is quite a long process, 
			spanning hours. For more information on why and when to use a compound file please refer to the 
			<a href="http://lucene.apache.org/java/docs/index.html" target="_top">Lucene</a> 	documentation.
		</td>
	</tr>
	<tr>
		<td class="td-content">batchSize</td>
		<td class="td-content"> 
			The number of records to retrieve from the database in each batch. For each batch there is a select on the database, increasing 
			the batch size means less selects and larger result sets. This does not have a very large performance influence unless the batch size 
			is 1 for example as selects are generally fast.
		</td>
	</tr>
	<tr>
		<td class="td-content">internetBatchSize</td>
		<td class="td-content"> 
			The batch size for the crawler. Each thread will get a batch of urls to read and index. Once again there is no real performance gain 
			between 100 and 1000 in the batch size.
		</td>
	</tr>
	<tr>
		<td class="td-content">maxReadLength</td>
		<td class="td-content"> 
			The maximum size to read from any resource, could be a blob in the database or a file on the file system. Indexing requires that 
			the data be stored in memory, certainly for PDF files for example. As such this parameter is quite important. In cases where there 
			is a PDF document of several hundred megabytes and 20 threads crawling the database, this would result in an out of  memory error. 
			Unfortunately this also means that there will be some data lost if the files are very big. Lucene can handle readers as input, which 
			can be on the file system but the performance loss is too great, certainly with large volumes is was found not to be practical. This 
			parameter should be set with due care. 
		</td>
	</tr>
	<tr>
		<td class="td-content">indexDirectoryPath</td>
		<td class="td-content"> 
			The path to the indexes. This path combined with the index name will determine the exact location on the file system where  
			the index is written. In the case where the directory path is for example ./indexes (as in the default configuration), Ikube will 
			create the folder in the working folder for the Jvm. This path can be on the network somewhere, provided the file share where 
			it is is accessible to the application. In the case of a cluster it is desirable to have all the servers write their part of the index to 
			a file share as this means that there will be only one copy of the index. If the index path is defined as ./indexes, then each server 
			will synchronize with the other servers, duplicating the index on each machine. Of course this facilitated complete failover in the 
			case where one server goes down, but if the indexes are 1 terabyte each then each server must have at least 2 terabytes of disk 
			space, one for the current index and one for the index being generated. 
		</td>
	</tr>
	<tr>
		<td class="td-content">indexables</td>
		<td class="td-content"> 
			These are the sources of data that will be indexed, like a web site and a database for example. They are defined as children 
			in the index context. Indexables and their definitions are described below.
		</td>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			Sources of data to index are defined as indexables. An indexable can be a url or a table structure in a database. Each indexable needs 
			to be defined separately. Indexables are added to the index context in the configuration, please refer to the 
			<a href="http://code.google.com/p/ikube/source/browse/#svn%2Ftrunk%2Fmodules%2Fcore%2Fsrc%2Fmain%2Fresources%2FMETA-INF" 
				target="_top">default configuration</a> 
			for an example. We'll start at the top with a database definition of an indexable. Tables are defined as beans with the following parameters:
		</td>
	</tr>
	<tr>
	<tr>
		<th colspan="2">IndexableTable definition parameters</th>
	</tr>
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td class="td-content">name</td>
		<td class="td-content"> 
			The name of the table. This will be used to generate the sql to access the table.
		</td>
	</tr>
	<tr>
		<td class="td-content">predicate</td>
		<td class="td-content"> 
			 This is an optional parameter where a predicate can be defined to limit the results. A typical example is 
			 where faq.faqid &lt; 10000. 
		</td>
	</tr>
		<tr>
		<td class="td-content">primary</td>
		<td class="td-content"> 
			Whether this table is a top level table. This will determine when the data collected while accessing the table 
			hierarchy will be written to the database.
		</td>
	</tr>
	<tr>
		<td class="td-content">dataSource</td>
		<td class="td-content"> 
			The reference to the datasource where the table is. The datasource must be defined in the Spring configuration, using 
			perhaps C3p0 as the pooled datasource provider.
		</td>
	</tr>
	<tr>
		<td class="td-content">children</td>
		<td class="td-content"> 
			The children of the table. Typically this is a list of columns and child tables.
		</td>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			As mentioned previously the sql to access the data is generated from the configuration. Tables can be nested within each 
			other as is normally the case with tables in a relational database. If a table is defined as a primary table and a child table is added 
			to the child indexables of the table then the logic to index the tables will be as follows:<br><br>
			
			1) Select the records from the primary table using the batch size defined in the index context and the predicate<br>
			2) Go to the next result in the result set for the table<br>
			3) Using the foreign key defined in the secondary table definition select the records from the second table<br> 
			4) Iterate over the second table results and index the data for the columns<br>
			5) Return to the primary table and move to the next result in the result set<br><br>
					
			Here is a solid example of the mechanism of indexing tables. We will index two tables, related to each other by a foreign key, the 'faq' 
			table and the 'attachments' table. The attachments table is 
			related to the faq table by a foreign key attachment.faqId =&gt; faq.id. We want to index all the data in the faq table and include all the 
			attachments for each faq. The logic during indexing is as follows.<br><br>
					
			1) select id, question, answer from faq where id &gt; 1000 and id &lt; 2000<br>
			2) ResultSet.next()<br>
			3) Extract column data and populate the Lucene document with the fields<br>
			4) select id, name, attachment from attachment where faq.id = 1000<br>
			5) ResultSet.next()<br>
			6) Extract column data and populate the Lucene document with the fields<br>
			7) Goto 5 until result set is depleted<br>
			8) Goto 2 until records are depleted<br>
			9) Goto 1, incrementing the batch, i.e. the id until there are no more records<br><br>
					
			The result of this is a Lucene document with the following fields and values:<br><br>
					
			&lt;{id=faq.1}, {question=where is Paris}, {answer=In France}, {name=documentOne.doc}, 
			{attachment=Paris and Lyon are both situated in France}&gt;<br><br>
					
			The configuration of tables can be arbitrarily complex, nesting depth can be up to 10 tables or more. Testing has only been done 
			to a depth of seven nested tables. In the unit test configuration the depth is four nested tables. With each level in the table hierarchy 
			there needs to be a select on the child table. This has a negative exponential performance effect on the indexing speed as with each 
			table there needs to be a select. For example:<br><br>
					
			* faq - 1000 records<br>
			* attachment - 10000 records, one for each faq<br> 
			* Version - 10000 records, one for each attachment<br><br>
				
			The selects on the database will be:<br><br>
				
			* faq - 1<br>
			* attachment - 1000<br>
			* version - 10000000<br><br>
					
			As you can see ten million selects on the version table could be time consuming. Increasing the size of the 
			faq table will have an exponential effect on the time it takes to index the data. In this case it could be interesting 
			to create two indexes, one for the versions and one for the faqs.
		</td>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			Columns are defined and added to the tables as children. Below is a table of parameters that can be defined for columns.
		</td>
	</tr>
	<tr>
	<tr>
		<th colspan="2">IndexableColumn definition parameters</th>
	</tr>
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td class="td-content">name</td>
		<td class="td-content"> 
			The name of the column.
		</td>
	</tr>
	<tr>
		<td class="td-content">idColumn</td>
		<td class="td-content"> 
			Whether this is the id column in the table.
		</td>
	</tr>
	<tr>
		<td class="td-content">nameColumn</td>
		<td class="td-content"> 
			This is used during indexing. For example in the case where there is a blob in the attachments table, and the name of the document 
			is in the 'name' column, this parameter is used to determine the mime type. If the name is document.doc, and there is a blob of the document 
			data then during indexing the .doc suffix will be used to get the correct parser to extract the text from the document, i.e. the Word parser.
		</td>
	</tr>
	<tr>
		<td class="td-content">fieldName</td>
		<td class="td-content"> 
			 The name of the field in the Lucene index. This allows columns to have separate field names, increasing the flexibility when searching. For 
			 example if there are timestamps for creation and they are defined as separate Lucene fields then searches like timestamp &gt; 12/12/2010 
			 AND timestamp &lt; 12/12/2011 are possible.
		</td>
	</tr>
	<tr>
		<td class="td-content">foreignKey</td>
		<td class="td-content"> 
			  The reference to the foreign key in the 'parent' table. This is used to select the records from the 'child' table referring to the parent id.
		</td>
	</tr>
	<tr>
		<td class="td-content">analyzed</td>
		<td class="td-content"> 
			A Lucene parameter, whether the data should be analyzed. Generally this is true.
		</td>
	</tr>
	<tr>
		<td class="td-content">stored</td>
		<td class="td-content"> 
			A Lucene parameter, whether the data should be stored in the index. Generally this is true. Of course if there is very large volumes of data 
			then storing the data could be prohibitively expensive, in terms of disk space and time. The write time of the index is a large proportion of the 
			indexing time.   
		</td>
	</tr>
	<tr>
		<td class="td-content">vectored</td>
		<td class="td-content"> 
			A Lucene parameter, whether the data should vectored. Generally this is true.
		</td>
	</tr>
	<tr>
		<td class="td-content" colspan="2"> 
			Please refer to the 
			<a href="http://code.google.com/p/ikube/source/browse/#svn%2Ftrunk%2Fcode%2Fcore%2Fsrc%2Fmain%2Fresources%2FMETA-INF"
				target="_top">default configuration</a> 
			for a complete example of a nested table configuration.
		</td>
	</tr>
	
	<tr>
		<th colspan="2">IndexableInternet definition parameters</th>
	</tr>
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td class="td-content">name</td>
		<td class="td-content"> 
			This is the name of the indexable, it should be unique within the configuration. It can be an arbitrary string.
		</td>
	</tr>
	<tr>
		<td class="td-content">url</td>
		<td class="td-content">
			The url of the site to index. Note that the host fragment of the url will be used as the base url 
			and the point to start the index. All pages and documents that are linked to this host or have the host as the 
			fragment in their url will be indexed.
		</td>
	</tr>
	<tr>
		<td class="td-content">idFieldName</td>
		<td class="td-content">
			The name of the field in the Lucene index for the identifier of this url. This is the field that will 
			be searched against when the index is created.
		</td>
	</tr>
	<tr>
		<td class="td-content">titleFieldName</td>
		<td class="td-content"> 
			As above with the id field name this is the field in the Lucene index that will be searched against 
			for the title of the document. In the case of an HTML page the title tag. In the case of a word document 
			the parser will attempt to extract the title from the document for this field and so on.
		</td>
	</tr>
	<tr>
		<td class="td-content">contentFieldName</td>
		<td class="td-content"> 
			The name of the lucene content field for the documents.
		</td>
	</tr>
	<tr>
		<td class="td-content">excludedPattern</td>
		<td class="td-content"> 
			Patterns that will be excluded from the indexing process. If there are files that should not be 
			indexed like images for example this can be used to exclude them from the indexing process.
		</td>
	</tr>
	<tr>
		<td class="td-content">analyzed</td>
		<td class="td-content"> 
			Whether the data will be analyzed by Lucene before being written to the index. Typically this 
			will be true. For more information on the Lucene parameters please refer to the Lucene documentation.
		</td>
	</tr>
	<tr>
		<td class="td-content">stored</td>
		<td class="td-content"> 
			Whether to store the data in the index. This will also typically be true as the fragment of text 
			returned by the search results will need the stored data to generate the fragment. However in the 
			case of very large document sets this will increase the index size considerably and my not be necessary.
		</td>
	</tr>
	<tr>
		<td class="td-content">vectored</td>
		<td class="td-content"> 
			Whether the data from the documents will be vectored by Lucene. Please refer to the Lucene 
			documentation for more details on this parameter.
		</td>
	</tr>
	
	<tr>
		<th colspan="2">IndexableEmail definition parameters</th>
	</tr>
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td class="td-content">mailHost</td>
		<td class="td-content">
			The mail host url to access the mail account.
		</td>
	</tr>
	<tr>
		<td class="td-content">name</td>
		<td class="td-content">
			The unique name of the indexable, this can be an arbitrary value.
		</td>
	</tr>
	<tr>
		<td class="td-content">port</td>
		<td class="td-content">
			The port to use for accessing the mail account. In the case of Gogole mail for example this is 995. This 
			has to be gotten from the mail provider.
		</td>
	</tr>
	<tr>
		<td class="td-content">protocol</td>
		<td class="td-content">
			The protocol to use. Also with Gmail the protocol is pop3 but is different for Hotmail and others, could be 
			imap for example.
		</td>
	</tr>
	<tr>
		<td class="td-content">secureSocketLayer</td>
		<td class="td-content">
			Whether to use secure sockets. Generally this will be true but need not be.
		</td>
	</tr>
	<tr>
		<td class="td-content">password</td>
		<td class="td-content">
			The password for the account.
		</td>
	</tr>
	<tr>
		<td class="td-content">username</td>
		<td class="td-content">
			The user account. In the case of the default mail account in the configuration this is ikube.ikube@gmail.com.
		</td>
	</tr>
	<tr>
		<td class="td-content">idField</td>
		<td class="td-content">
			The id field name in the Lucene index for the identifier of the message. The id is a concatenation of the 
			mail account, the message number and the user name.
		</td>
	</tr>
	<tr>
		<td class="td-content">contentField</td>
		<td class="td-content">
			The field name of the content field in the Lucene index. This is where the message data like the content and 
			the header will be added to the index. 
		</td>
	</tr>
	<tr>
		<td class="td-content">titleField</td>
		<td class="td-content">
			The name of the title field in the Lucene index.
		</td>
	</tr>
	<tr>
		<td class="td-content"></td>
		<td class="td-content"> 
		</td>
	</tr>
	
	<tr>
		<th colspan="2">IndexableFileSystem definition parameters</th>
	</tr>
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td class="td-content">name</td>
		<td class="td-content">
			The uniqiue name of this indexable in the configuration.
		</td>
	</tr>
	<tr>
		<td class="td-content">path</td>
		<td class="td-content">
			The absolute or relative path to the file or folder to index. This can be accross the network 
			provided the drive is mapped to the machine where Ikube is running.
		</td>
	</tr>
	<tr>
		<td class="td-content">pathFieldName</td>
		<td class="td-content">
			The name in the Lucene index of the path to the file that is being indexed.
		</td>
	</tr>
	<tr>
		<td class="td-content">nameFieldName</td>
		<td class="td-content">
			The name in the Lucene index of the name of the file.
		</td>
	</tr>
	<tr>
		<td class="td-content">lengthFieldname</td>
		<td class="td-content">
			The name of the field in the Lucene index of the length of the file, ie. the size of it. 
		</td>
	</tr>
	<tr>
		<td class="td-content">contentFieldName</td>
		<td class="td-content">
			The name of the field in the Lucene index for the fiel content. This is typically the important field 
			that will be searched once the index is created.
		</td>
	</tr>
	<tr>
		<td class="td-content">excludedPattern</td>
		<td class="td-content">
			Any excluded patterns that whould be excldued from the indexing process, like for example 
			exe files and video as Ikube can't index video just yet, although there is some investigation into 
			this at the moment.
		</td>
	</tr>
	<tr>
		<td class="td-content">lastModifiedFieldName</td>
		<td class="td-content">
			The name of the field in the Lucene index for the last modified timestamp of the file being indexed.
		</td>
	</tr>
</table>
