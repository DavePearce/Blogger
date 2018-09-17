package blogger;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.apache.http.ConnectionClosedException;
import org.apache.http.ExceptionLogger;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;

import blogger.pages.FrontPage;
import blogger.pages.PostPages;
import jwebkit.http.HttpFileHandler;
import jwebkit.sql.*;

import static jwebkit.sql.SqlValue.*;

public class Main {

	public static final int HTTP_PORT = 8080;

	public static final ContentType TEXT_JAVASCRIPT = ContentType.create("text/javascript");
	public static final ContentType TEXT_CSS = ContentType.create("text/css");

	public static class Post extends AbstractSqlRow {
		public static final SqlTable.Column[] schema = {
				// Unique ID of post
				new SqlTable.Column("ID", SqlType.INT),
				// Title of the post
				new SqlTable.Column("title", SqlType.TINYTEXT),
				// Link is component of perma-link
				new SqlTable.Column("link", SqlType.TINYTEXT),
				// Contents of post (HTML)
				new SqlTable.Column("body", SqlType.LONGTEXT),
				// Exerpt
				new SqlTable.Column("exerpt", SqlType.TEXT),
				// Publication date
				new SqlTable.Column("datetime", SqlType.DATETIME)
		};

		public Post(int id, String title, String link, String body, String exerpt, LocalDateTime datetime) {
			super(Int(id), new SqlValue.Text(title), new SqlValue.Text(link), new SqlValue.Text(body),  new SqlValue.Text(exerpt), new SqlValue.DateTime(datetime));
		}

		public Post(SqlValue...values) {
			super(values);
		}

		public int ID() {
			return ((SqlValue.Int) get(0)).asInt();
		}

		public String title() {
			return ((SqlValue.Text) get(1)).asString();
		}

		public String link() {
			return ((SqlValue.Text) get(2)).asString();
		}

		public String body() {
			return ((SqlValue.Text) get(3)).asString();
		}

		public String excerpt() {
			return ((SqlValue.Text) get(4)).asString();
		}

		public LocalDateTime date() {
			return ((SqlValue.DateTime) get(5)).asLocalDateTime();
		}
	}

	public static void main(String[] args) throws SQLException, IOException {
		// First, read in the username and password
//		String username = readString("Username: ");
//		String password = readString("Password: ");
		String username = System.getenv("USERNAME");
		String password = System.getenv("PASSWORD");
		// Second, create the database connection
		Connection connection = getSqlLiteDatabaseConnection();
		//Connection connection = getMySqlDatabaseConnection(username,new String(password));
		SqlDatabase db = new SqlDatabase(connection);
		SqlTable<Post> posts = new SqlTable<>(db,"posts",Post.class,Post.schema);

		SocketConfig socketConfig = SocketConfig.custom()
				.setSoTimeout(15000)
				.setTcpNoDelay(true)
				.build();

		HttpServer server = ServerBootstrap.bootstrap()
				.setListenerPort(HTTP_PORT)
				.setServerInfo("Test/1.1")
				.setSocketConfig(socketConfig)
				.setExceptionLogger(new Logger())
				.registerHandler("/css/*", new HttpFileHandler(new File("."),TEXT_CSS))
				.registerHandler("/js/*", new HttpFileHandler(new File("."),TEXT_JAVASCRIPT))
				.registerHandler("/", new FrontPage(posts))
				.registerHandler("/*", new PostPages(posts))
				.create();
		try {
			if(!posts.exists()) {
				posts.create();
			}
			server.start();
			//server.awaitTermination(-1, TimeUnit.MILLISECONDS);
		} catch(IOException e) {
			e.printStackTrace();
		}
		// Now, add something to users
	}

	private static class Logger implements ExceptionLogger {

		@Override
		public void log(Exception ex) {
            if (ex instanceof SocketTimeoutException) {
                System.err.println(ex.getMessage());
            } else if (ex instanceof ConnectionClosedException) {
                System.err.println(ex.getMessage());
            } else {
                ex.printStackTrace();
            }
		}
    }

	private static Connection getSqlLiteDatabaseConnection() throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (Exception e) {
			e.printStackTrace();
		}
		Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
		return conn;
	}

	private static Connection getMySqlDatabaseConnection(String user, String password) throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/blogger" +
                                   "?user=" + user + "&password=" + password);
		return conn;
	}

	private static String readString(String text) throws IOException {
		System.out.print(text);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		return bufferedReader.readLine();
	}
}
