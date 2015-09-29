package de.mygrades.database.dao;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

import de.mygrades.database.dao.Action;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ACTION".
*/
public class ActionDao extends AbstractDao<Action, Long> {

    public static final String TABLENAME = "ACTION";

    /**
     * Properties of entity Action.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property ActionId = new Property(0, Long.class, "actionId", true, "ACTION_ID");
        public final static Property Position = new Property(1, int.class, "position", false, "POSITION");
        public final static Property Method = new Property(2, String.class, "method", false, "METHOD");
        public final static Property Url = new Property(3, String.class, "url", false, "URL");
        public final static Property ParseExpression = new Property(4, String.class, "parseExpression", false, "PARSE_EXPRESSION");
        public final static Property RuleId = new Property(5, long.class, "ruleId", false, "RULE_ID");
    };

    private DaoSession daoSession;

    private Query<Action> rule_ActionsQuery;

    public ActionDao(DaoConfig config) {
        super(config);
    }
    
    public ActionDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ACTION\" (" + //
                "\"ACTION_ID\" INTEGER PRIMARY KEY ," + // 0: actionId
                "\"POSITION\" INTEGER NOT NULL ," + // 1: position
                "\"METHOD\" TEXT NOT NULL ," + // 2: method
                "\"URL\" TEXT," + // 3: url
                "\"PARSE_EXPRESSION\" TEXT," + // 4: parseExpression
                "\"RULE_ID\" INTEGER NOT NULL );"); // 5: ruleId
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ACTION\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Action entity) {
        stmt.clearBindings();
 
        Long actionId = entity.getActionId();
        if (actionId != null) {
            stmt.bindLong(1, actionId);
        }
        stmt.bindLong(2, entity.getPosition());
        stmt.bindString(3, entity.getMethod());
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(4, url);
        }
 
        String parseExpression = entity.getParseExpression();
        if (parseExpression != null) {
            stmt.bindString(5, parseExpression);
        }
        stmt.bindLong(6, entity.getRuleId());
    }

    @Override
    protected void attachEntity(Action entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Action readEntity(Cursor cursor, int offset) {
        Action entity = new Action( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // actionId
            cursor.getInt(offset + 1), // position
            cursor.getString(offset + 2), // method
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // url
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // parseExpression
            cursor.getLong(offset + 5) // ruleId
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Action entity, int offset) {
        entity.setActionId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setPosition(cursor.getInt(offset + 1));
        entity.setMethod(cursor.getString(offset + 2));
        entity.setUrl(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setParseExpression(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setRuleId(cursor.getLong(offset + 5));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Action entity, long rowId) {
        entity.setActionId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Action entity) {
        if(entity != null) {
            return entity.getActionId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "actions" to-many relationship of Rule. */
    public List<Action> _queryRule_Actions(long ruleId) {
        synchronized (this) {
            if (rule_ActionsQuery == null) {
                QueryBuilder<Action> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.RuleId.eq(null));
                rule_ActionsQuery = queryBuilder.build();
            }
        }
        Query<Action> query = rule_ActionsQuery.forCurrentThread();
        query.setParameter(0, ruleId);
        return query.list();
    }

}
