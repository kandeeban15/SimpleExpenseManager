package lk.ac.mrt.cse.dbs.simpleexpensemanager.control;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class DatabaseHelper extends SQLiteOpenHelper {
    private final Context context;

    private final static String DATABASE_NAME = "190296T.db";

    private final static  String ACCOUNTS_TABLE = "AccountDetails";

    private final static  String ACCOUNT_NO = "AccountNo";
    private final static  String BANK = "Bank";
    private final static  String ACCOUNT_HOLDER = "AccountHolder";
    private final static  String INITIAL_BALANCE = "InitialBalance";

    private final static  String LOGS_TABLE = "TransactionLogs";

    private final static  String LOG_ID = "LogID";
    private final static  String DATE = "Date";
    private final static  String EXPENSE_TYPE = "Type";
    private final static  String AMOUNT = "Amount";

    private static SQLiteDatabase db;
    public DatabaseHelper(@Nullable Context context) {
        //creates a database with version 1
        super(context,DATABASE_NAME, null,1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query1 = "CREATE TABLE IF NOT EXISTS " +
                ACCOUNTS_TABLE +
                "( " +ACCOUNT_NO +" TEXT NOT NULL UNIQUE," +
                BANK+" TEXT NOT NULL," +
                ACCOUNT_HOLDER+" TEXT NOT NULL," +
                INITIAL_BALANCE+" NUMERIC," +
                "PRIMARY KEY(AccountNo));";
        String query2 = "CREATE TABLE IF NOT EXISTS " +
                LOGS_TABLE +
                "(" +
                LOG_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                DATE+" TEXT NOT NULL," +
                ACCOUNT_NO+" TEXT NOT NULL,"+
                EXPENSE_TYPE+" TEXT NOT NULL," +
                AMOUNT+" NUMERIC NOT NULL," +
                "FOREIGN KEY(" +ACCOUNT_NO +") REFERENCES " +ACCOUNTS_TABLE+"("+ACCOUNT_NO+"));";
        db.execSQL(query1);
        db.execSQL(query2);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " +ACCOUNTS_TABLE +";");
        db.execSQL("DROP TABLE IF EXISTS " + LOGS_TABLE + ";");
        onCreate(db);
    }

    public void addAccount(Account account) {
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ACCOUNT_NO,account.getAccountNo());
        cv.put(BANK,account.getBankName());
        cv.put(ACCOUNT_HOLDER,account.getAccountHolderName());
        cv.put(INITIAL_BALANCE,account.getBalance());
        long success = db.insert(ACCOUNTS_TABLE,null,cv);
//        if (success==-1){
//            Toast.makeText(context, "Account Added Successfully", Toast.LENGTH_SHORT).show();
//        }else{
//            Toast.makeText(context, "Account Not Added", Toast.LENGTH_SHORT).show();
//        }

    }
    public void removeAccount(String accountNo) {
        db = this.getWritableDatabase();
        long success = db.delete(ACCOUNTS_TABLE, ACCOUNT_NO+"=?", new String[]{accountNo});
        if (success==-1){
            Toast.makeText(context, "Account Removal Failed", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Account Removed", Toast.LENGTH_SHORT).show();
        }
        db.close();

    }

    public Map<String,Account> getAccounts() {
        String selectQuery = "SELECT  * FROM " + ACCOUNTS_TABLE;
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Map<String,Account> accounts= new HashMap<>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Account account = new Account(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getDouble(3));
            accounts.put(cursor.getString(0),account);
            cursor.moveToNext();
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        db.close();
        return accounts;

    }
    public void updateAccount(Account account) {
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ACCOUNT_NO,account.getAccountNo());
        cv.put(BANK,account.getBankName());
        cv.put(ACCOUNT_HOLDER,account.getAccountHolderName());
        cv.put(INITIAL_BALANCE,account.getBalance());
        long success = db.update(ACCOUNTS_TABLE,cv, ACCOUNT_NO+"=?", new String[]{account.getAccountNo()});
//        if (success==-1){
//            Toast.makeText(context, "DB UPDATE FAILED", Toast.LENGTH_SHORT).show();
//        }else{
//            Toast.makeText(context, "DB UPDATE SUCCEEDED", Toast.LENGTH_SHORT).show();
//        }

    }


    //Transactions
    public List<Transaction> getTransactions() {
        String selectQuery = "SELECT  * FROM " + LOGS_TABLE;
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        List<Transaction> transactions= new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String[] dates = cursor.getString(1).split("-");
            Date date= new Date(Integer.parseInt(dates[2])-1900,Integer.parseInt(dates[1]),Integer.parseInt(dates[0]));
            ExpenseType type = null;
            if (cursor.getString(3).equals("EXPENSE")){
                type = ExpenseType.EXPENSE;
            }else if (cursor.getString(3).equals("INCOME")){
                type = ExpenseType.INCOME;
            }else{
                Toast.makeText(context, "EXPENSE TYPE UNRECOGNIZED", Toast.LENGTH_SHORT).show();
            }

            Transaction account = new Transaction(date,cursor.getString(2),type,cursor.getDouble(4));
            transactions.add(account);//using accountNo
            cursor.moveToNext();
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        db.close();
        return transactions;

    }

    public void addTransactionLog(Transaction transaction) {
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        Date date = transaction.getDate();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String strDate = dateFormat.format(date);
        cv.put(DATE,strDate);

        cv.put(ACCOUNT_NO,transaction.getAccountNo());

        String type = "UNKNOWN";
        if (transaction.getExpenseType()==ExpenseType.EXPENSE){
            type = "EXPENSE";
        }else if (transaction.getExpenseType()==ExpenseType.INCOME){
            type = "INCOME";
        }
        cv.put(EXPENSE_TYPE,type);

        cv.put(AMOUNT,transaction.getAmount());

        long success = db.insert(LOGS_TABLE,null,cv);
        if (success==-1){
            Toast.makeText(context, "TRANSACTION UPDATE FAILED", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "TRANSACTION UPDATE SUCCEEDED", Toast.LENGTH_SHORT).show();
        }

    }

}
