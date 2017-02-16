package blogger;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
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
import jwebkit.http.HttpFileHandler;
import jwebkit.sql.*;

import static jwebkit.sql.SqlValue.*;

public class Main {

	public static final int HTTP_PORT = 8080;

	public static final ContentType TEXT_CSS = ContentType.create("text/css");

	public static class Post extends SqlRow {
		public Post(int id, String body, LocalDate date) {
			super(Int(id), new SqlValue.Text(body), new SqlValue.Date(date));
		}

		public int ID() {
			return ((SqlValue.Int) get(0)).asInt();
		}

		public String name() {
			return ((SqlValue.Text) get(1)).asString();
		}

		public LocalDate date() {
			return ((SqlValue.Date) get(2)).asLocalDate();
		}
	}

	/**
	 * Define the Schema for the Users table.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Posts extends SqlTable<Post> {

		public Posts(SqlDatabase db) {
			super(db, "posts",
					new SqlTable.Column("ID", SqlType.INT),
					new SqlTable.Column("body", SqlType.VARCHAR(20)),
					new SqlTable.Column("date", SqlType.DATE));
		};

		@Override
		public Post construct(Object... values) {
			System.out.println("ARRAY: " + Arrays.toString(values));
			LocalDate date = LocalDate.parse((String)values[2]);
			return new Post((Integer) values[0],(String)values[1], date);
		}

	}

	public static void main(String[] args) throws SQLException {
		Connection connection = getDatabaseConnection();
		SqlDatabase db = new SqlDatabase(connection);
		Posts posts = new Posts(db);

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
				.registerHandler("/", new FrontPage(posts))
				.create();

		try {
			if (!posts.exists()) {
				posts.create();
				posts.insert(new Post(1, "Hello Dave", LocalDate.now()));
				posts.insert(new Post(1, "Hello Teddy", LocalDate.now()));
			}
			server.start();
			//server.awaitTermination(-1, TimeUnit.MILLISECONDS);
		} catch(IOException e) {
			e.printStackTrace();
		}

		// Now, add something to users
		//users.insert(new Users.Row((Integer) 1,"James"));
		//users.delete(new Users.Row((Integer) 1,"James"));
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

	private static Connection getDatabaseConnection() throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (Exception e) {
			e.printStackTrace();
		}
		Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
		return conn;
	}
}
