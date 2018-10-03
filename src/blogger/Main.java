package blogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.apache.http.ConnectionClosedException;
import org.apache.http.ExceptionLogger;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;

import blogger.core.Comments.Comment;
import blogger.core.Comments;
import blogger.core.Posts;
import blogger.core.Posts.Post;
import blogger.pages.FrontPage;
import blogger.pages.PostPages;
import jwebkit.http.HttpFileHandler;
import jwebkit.sql.*;

import static jwebkit.sql.SqlValue.*;

public class Main {

	public static final int[] HTTP_PORTS = {80,8080,8081};

	public static final ContentType TEXT_JAVASCRIPT = ContentType.create("text/javascript");
	public static final ContentType TEXT_CSS = ContentType.create("text/css");

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
		Posts posts = new Posts(db);
		Comments comments = new Comments(db);
		//
		SocketConfig socketConfig = SocketConfig.custom()
				.setSoTimeout(15000)
				.setTcpNoDelay(true)
				.build();
		// Set port number from which we'll try to run the server. If this port
		// is taken, we'll try the next one and the next one, until we find a
		// match.
		int portIndex = 0;
		while (portIndex < HTTP_PORTS.length) {
			int port = HTTP_PORTS[portIndex++];
			try {
				HttpServer server = ServerBootstrap.bootstrap()
						.setListenerPort(port)
						.setServerInfo("Test/1.1")
						.setSocketConfig(socketConfig)
						.setExceptionLogger(new Logger())
						.registerHandler("/css/*", new HttpFileHandler(new File("."),TEXT_CSS))
						.registerHandler("/js/*", new HttpFileHandler(new File("."),TEXT_JAVASCRIPT))
						.registerHandler("/", new FrontPage(posts))
						.registerHandler("/*", new PostPages(posts, comments))
						.create();

				if(!posts.exists()) {
					System.out.println("CREATING POSTS TABLE");
					//
					posts.create();
					posts.insert(new Post(0,"This is a post","url","Hi dave this is a post","caption!",LocalDateTime.now()));
				}
				if(!comments.exists()) {
					System.out.println("CREATING COMMENTS TABLE");
					comments.create();
					comments.insert(new Comment(0,0,"Hi dave, this is an interesting post",LocalDateTime.now()));
				}
				server.start();
				//server.awaitTermination(-1, TimeUnit.MILLISECONDS);
			} catch (BindException e) {
				System.out.println("Port " + port + " in use by another application.");
			} catch(IOException e) {
				e.printStackTrace();
			}
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
