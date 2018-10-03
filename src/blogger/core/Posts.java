package blogger.core;

import static jwebkit.sql.SqlValue.Int;

import java.time.LocalDateTime;

import jwebkit.sql.AbstractSqlRow;
import jwebkit.sql.SqlDatabase;
import jwebkit.sql.SqlTable;
import jwebkit.sql.SqlType;
import jwebkit.sql.SqlValue;

public class Posts extends SqlTable<Posts.Post> {

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

	public Posts(SqlDatabase db) {
		super(db, "posts", Post.class, schema);
	}

	public static class Post extends AbstractSqlRow {

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

}
