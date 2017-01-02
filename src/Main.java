import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

import jwebkit.http.HttpMethodDispatchHandler;
import jwebkit.sql.*;

import static jwebkit.sql.SqlValue.*;

public class Main {

	public static final int HTTP_PORT = 8080;

	/**
	 * Define the Schema for the Users table.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Posts extends SqlSchema<Posts.Row> {

		public Posts(){
			super(new SqlSchema.Column("ID", SqlType.INT),
				  new SqlSchema.Column("body", SqlType.VARCHAR(20)));
		};

		@Override
		public Row construct(Object... values) {
			return new Row((Integer) values[0],(String)values[1]);
		}

		public static class Row extends SqlRow {
			public Row(int id, String name) {
				super(Int(id), new SqlValue.Text(name));
			}

			public int ID() {
				return ((SqlValue.Int) get(0)).asInt();
			}

			public String name() {
				return ((SqlValue.Text) get(1)).asString();
			}
		}
	}

	public static void main(String[] args) throws SQLException {
		Connection connection = getDatabaseConnection();
		SqlDatabase db = new SqlDatabase(connection);
		db.bindTable("users", new Posts());
		SqlTable<Posts.Row> posts = db.getTable("users");

		SocketConfig socketConfig = SocketConfig.custom()
				.setSoTimeout(15000)
				.setTcpNoDelay(true)
				.build();

		HttpServer server = ServerBootstrap.bootstrap()
				.setListenerPort(HTTP_PORT)
				.setServerInfo("Test/1.1")
				.setSocketConfig(socketConfig)
				.setExceptionLogger(new Logger())
				.registerHandler("/", new FrontPage(posts))
				.create();

		try {
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

	public static class FrontPage extends HttpMethodDispatchHandler {
		private final SqlTable<Posts.Row> posts;

		public FrontPage(SqlTable<Posts.Row> posts) {
			super(HttpMethodDispatchHandler.ALLOW_GET);
			this.posts = posts;
		}

		@Override
		public void get(HttpRequest request, HttpResponse response, HttpContext context)
				throws HttpException, IOException {
			String uri = request.getRequestLine().getUri();
			try {
				List<NameValuePair> parameters = new URIBuilder(uri).getQueryParams();
				ByteArrayOutputStream ous = new ByteArrayOutputStream();
				PrintWriter writer = new PrintWriter(ous);
				writePage(writer);
				writer.flush();
				response.setStatusCode(HttpStatus.SC_OK);
				response.setEntity(new ByteArrayEntity(ous.toByteArray(), ContentType.TEXT_HTML));
			} catch(URISyntaxException e) {
				throw new HttpException("Invalid URI",e);
			}
		}

		private void writePage(PrintWriter writer) {
			writer.println("<!DOCTYPE html>");
			writer.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\" lang=\"en-US\">");
			writeHeader(writer);
			writeBody(writer);
			writer.println("</html>");
		}

		public void writeHeader(PrintWriter writer) {
			writer.println("<head>");
			writer.println("<title>Blogger Test</title>");
			writer.println("</head>");

		}

		public void writeBody(PrintWriter writer) {
			writer.println("<body>");
			writer.println("<center>");
			writer.println("<table border=\"1px\" width=\"80%\">");
			for(Posts.Row row : posts.select()) {
				writer.println("<tr>");
				writer.println("<td>" + row.ID() + "</td>");
				writer.println("<td>" + row.name() + "</td>");
				writer.println("</tr>");
			}
			writer.println("</table>");
			writer.println("</center>");
			writer.println("</body>");
		}
	}
}
