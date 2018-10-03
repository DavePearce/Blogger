package blogger.core;

import static jwebkit.sql.SqlValue.Int;

import java.time.LocalDateTime;
import java.util.Iterator;

import blogger.core.Posts.Post;
import jwebkit.sql.AbstractSqlRow;
import jwebkit.sql.SqlDatabase;
import jwebkit.sql.SqlTable;
import jwebkit.sql.SqlType;
import jwebkit.sql.SqlValue;

public class Comments extends SqlTable<Comments.Comment> {

	public static final SqlTable.Column[] schema = {
			// Unique ID of comment
			new SqlTable.Column("ID", SqlType.INT),
			// Parent post (ID)
			new SqlTable.Column("post", SqlType.INT),
			// Contents of comment (HTML)
			new SqlTable.Column("body", SqlType.LONGTEXT),
			// Publication date
			new SqlTable.Column("datetime", SqlType.DATETIME)
	};

	public Comments(SqlDatabase db) {
		super(db, "comments", Comment.class, schema);
	}

	public Iterable<Comment> getCommentsForPost(Post p) {
		return select().whereEqual(schema[1], p.get(0));
	}

	public static class Comment extends AbstractSqlRow {

		public Comment(int id, int parent, String body, LocalDateTime datetime) {
			super(Int(id), Int(parent), new SqlValue.Text(body),  new SqlValue.DateTime(datetime));
		}

		public Comment(SqlValue...values) {
			super(values);
		}

		public int ID() {
			return ((SqlValue.Int) get(0)).asInt();
		}

		public int post() {
			return ((SqlValue.Int) get(1)).asInt();
		}

		public String body() {
			return ((SqlValue.Text) get(2)).asString();
		}

		public LocalDateTime date() {
			return ((SqlValue.DateTime) get(3)).asLocalDateTime();
		}
	}
}
