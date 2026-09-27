package cn.ppps.forwarder.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cn.ppps.forwarder.database.dao.FrpcDao
import cn.ppps.forwarder.database.dao.LogsDao
import cn.ppps.forwarder.database.dao.MsgDao
import cn.ppps.forwarder.database.dao.RuleDao
import cn.ppps.forwarder.database.dao.SenderDao
import cn.ppps.forwarder.database.dao.TaskDao
import cn.ppps.forwarder.database.entity.Frpc
import cn.ppps.forwarder.database.entity.Logs
import cn.ppps.forwarder.database.entity.LogsDetail
import cn.ppps.forwarder.database.entity.Msg
import cn.ppps.forwarder.database.entity.Rule
import cn.ppps.forwarder.database.entity.Sender
import cn.ppps.forwarder.database.entity.Task
import cn.ppps.forwarder.database.ext.ConvertersDate
import cn.ppps.forwarder.utils.DATABASE_NAME
import cn.ppps.forwarder.utils.SettingUtils
import cn.ppps.forwarder.utils.TAG_LIST

@Database(
    entities = [Frpc::class, Msg::class, Logs::class, Rule::class, Sender::class, Task::class],
    views = [LogsDetail::class],
    version = 21,
    exportSchema = false
)
@TypeConverters(ConvertersDate::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun frpcDao(): FrpcDao
    abstract fun msgDao(): MsgDao
    abstract fun logsDao(): LogsDao
    abstract fun ruleDao(): RuleDao
    abstract fun senderDao(): SenderDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, DATABASE_NAME
            ).allowMainThreadQueries()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        //fillInDb(context.applicationContext)
                        db.execSQL(
                            """
INSERT INTO "Frpc" VALUES ('830b0a0e-c2b3-4f95-b3c9-55db12923d2e', 'SMS 자동전달 원격 제어', '[common]
#frps 서버 공인 IP
server_addr = 88.88.88.88
#frps 서버 공인 포트
server_port = 8888
#선택 사항: 인증 사용을 권장합니다
token = 88888888
#서버 연결 제한 시간: 네트워크 준비 전에 시작할 경우 충분히 길게 설정합니다
dial_server_timeout = 60
#최초 로그인 실패 시 종료 여부
login_fail_exit = false

#다음 두 방식 중 하나를 선택합니다. 기기별로 고유하게 설정하며 http://88.88.88.88:5000 으로 접속합니다
[SmsForwarder-TCP]
type = tcp
local_ip = 127.0.0.1
local_port = 5000
#아래 값을 수정합니다. frps 서버에서 외부 접근을 허용할 포트입니다
remote_port = 5000

#다음 두 방식 중 하나를 선택합니다. 기기별로 고유하게 설정하며 http://smsf.demo.com 으로 접속합니다
[SmsForwarder-HTTP]
type = http
local_ip = 127.0.0.1
local_port = 5000
#아래 값을 수정합니다. frps 서버에서 이 도메인을 vhost_http_port로 프록시합니다
custom_domains = smsf.demo.com
', 0, '1651334400000')
""".trimIndent()
                        )
                    }
                }).addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13,
                    MIGRATION_13_14,
                    MIGRATION_14_15,
                    MIGRATION_15_16,
                    MIGRATION_16_17,
                    MIGRATION_17_18,
                    MIGRATION_18_19,
                    MIGRATION_19_20,
                    MIGRATION_20_21,
                )

            /*if (BuildConfig.DEBUG) {
                builder.setQueryCallback({ sqlQuery, bindArgs ->
                    println("SQL_QUERY: $sqlQuery\nBIND_ARGS: $bindArgs")
                }, Executors.newSingleThreadExecutor())
            }*/

            return builder.build()
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table log add column sim_info TEXT ")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table rule add column sim_slot TEXT NOT NULL DEFAULT 'ALL' ")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table log add column forward_status INTEGER NOT NULL DEFAULT 1 ")
                database.execSQL("Alter table log add column forward_response TEXT NOT NULL DEFAULT 'ok' ")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table rule add column sms_template TEXT NOT NULL DEFAULT '' ")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table rule add column type TEXT NOT NULL DEFAULT 'sms' ")
                database.execSQL("Alter table log add column type TEXT NOT NULL DEFAULT 'sms' ")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table rule add column regex_replace TEXT NOT NULL DEFAULT '' ")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("update log set forward_status = 2 where forward_status = 1 ")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table rule add column status INTEGER NOT NULL DEFAULT 1 ")
                database.execSQL("update sender set status = 1 ")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
CREATE TABLE "Frpc" (
  "uid" TEXT NOT NULL,
  "name" TEXT NOT NULL,
  "config" TEXT NOT NULL,
  "autorun" INTEGER NOT NULL DEFAULT 0,
  "time" INTEGER NOT NULL,
  PRIMARY KEY ("uid")
)
""".trimIndent()
                )
                database.execSQL(
                    """
INSERT INTO "Frpc" VALUES ('830b0a0e-c2b3-4f95-b3c9-55db12923d2e', 'SMS 자동전달 원격 제어', '
#frps 서버 공인 IP
serverAddr = "88.88.88.88"
#frps 서버 공인 포트
serverPort = 8888
#서버 연결 제한 시간: 네트워크 준비 전에 시작할 경우 충분히 길게 설정합니다
transport.dialServerTimeout = 60
#최초 로그인 실패 시 종료 여부
loginFailExit = false
#선택 사항: 인증 사용을 권장합니다
auth.method = "token"
auth.token = "88888888"

#두 방식 중 하나를 선택합니다. name과 remotePort는 기기별로 고유해야 합니다. http://88.88.88.88:5000 으로 접속합니다
[[proxies]]
#동일한 frps 서버에서 각 기기의 name은 고유해야 합니다
name = "SmsForwarder-TCP-001"
type = "tcp"
localIP = "127.0.0.1"
localPort = 5000
#아래 값을 수정합니다. frps 서버에서 외부 접근과 방화벽 통과를 허용할 고유한 포트입니다
remotePort = 5000

#두 방식 중 하나를 선택합니다. name과 customDomains는 기기별로 고유해야 합니다. http://smsf.demo.com 으로 접속합니다
[[proxies]]
#동일한 frps 서버에서 각 기기의 name은 고유해야 합니다
name = "SmsForwarder-HTTP-001"
type = "http"
localPort = 5000
#아래 값을 수정합니다. frps 서버에서 이 도메인을 vhost_http_port로 프록시합니다
customDomains = ["smsf.demo.com"]

', 0, '1651334400000')
""".trimIndent()
                )

                database.execSQL("ALTER TABLE log RENAME TO old_log")
                database.execSQL(
                    """
CREATE TABLE "Logs" (
  "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "type" TEXT NOT NULL DEFAULT 'sms',
  "from" TEXT NOT NULL DEFAULT '',
  "content" TEXT NOT NULL DEFAULT '',
  "rule_id" INTEGER NOT NULL DEFAULT 0,
  "sim_info" TEXT NOT NULL DEFAULT '',
  "forward_status" INTEGER NOT NULL DEFAULT 1,
  "forward_response" TEXT NOT NULL DEFAULT '',
  "time" INTEGER NOT NULL,
  FOREIGN KEY ("rule_id") REFERENCES "Rule" ("id") ON DELETE CASCADE ON UPDATE CASCADE
)
""".trimIndent()
                )
                database.execSQL("CREATE UNIQUE INDEX \"index_Log_id\" ON \"Logs\" ( \"id\" ASC)")
                database.execSQL("CREATE INDEX \"index_Log_rule_id\" ON \"Logs\" ( \"rule_id\" ASC)")
                database.execSQL("INSERT INTO Logs (id,type,`from`,content,sim_info,rule_id,forward_status,forward_response,time) SELECT _id,type,l_from,content,sim_info,rule_id,forward_status,forward_response,strftime('%s000',time) FROM old_log")
                database.execSQL("DROP TABLE old_log")

                database.execSQL("ALTER TABLE rule RENAME TO old_rule")
                database.execSQL(
                    """
CREATE TABLE "Rule" (
  "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "type" TEXT NOT NULL DEFAULT 'sms',
  "filed" TEXT NOT NULL DEFAULT 'transpond_all',
  "check" TEXT NOT NULL DEFAULT 'is',
  "value" TEXT NOT NULL DEFAULT '',
  "sender_id" INTEGER NOT NULL DEFAULT 0,
  "sms_template" TEXT NOT NULL DEFAULT '',
  "regex_replace" TEXT NOT NULL DEFAULT '',
  "sim_slot" TEXT NOT NULL DEFAULT 'ALL',
  "status" INTEGER NOT NULL DEFAULT 1,
  "time" INTEGER NOT NULL,
  FOREIGN KEY ("sender_id") REFERENCES "Sender" ("id") ON DELETE CASCADE ON UPDATE CASCADE
)
""".trimIndent()
                )
                database.execSQL("CREATE UNIQUE INDEX \"index_Rule_id\" ON \"Rule\" ( \"id\" ASC)")
                database.execSQL("CREATE INDEX \"index_Rule_sender_id\" ON \"Rule\" ( \"sender_id\" ASC)")
                database.execSQL("INSERT INTO Rule (id,type,filed,`check`,value,sender_id,time,sms_template,regex_replace,status,sim_slot) SELECT _id,type,filed,tcheck,value,sender_id,strftime('%s000',time),sms_template,regex_replace,status,sim_slot FROM old_rule")
                database.execSQL("DROP TABLE old_rule")

                database.execSQL("ALTER TABLE sender RENAME TO old_sender")
                database.execSQL(
                    """
CREATE TABLE "Sender" (
  "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "type" INTEGER NOT NULL DEFAULT 1,
  "name" TEXT NOT NULL DEFAULT '',
  "json_setting" TEXT NOT NULL DEFAULT '',
  "status" INTEGER NOT NULL DEFAULT 1,
  "time" INTEGER NOT NULL
)
""".trimIndent()
                )
                database.execSQL("INSERT INTO Sender (id,name,status,type,json_setting,time) SELECT _id,name,status,type,json_setting,strftime('%s000',time) FROM old_sender")
                database.execSQL("DROP TABLE old_sender")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table Logs add column sub_id INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table Logs add column sender_id INTEGER NOT NULL DEFAULT 0")
                database.execSQL("Update Logs Set sender_id = (Select sender_id from Rule where Logs.rule_id = Rule.id)")
                database.execSQL("Alter table Rule add column sender_list TEXT NOT NULL DEFAULT ''")
                database.execSQL("Update Rule set sender_list = sender_id")
                database.execSQL("CREATE INDEX \"index_Rule_sender_ids\" ON \"Rule\" ( \"sender_list\" ASC)")
                /*database.execSQL("Create table Rule_t as Select id,type,filed,check,value,sender_list,sms_template,regex_replace,sim_slot,status,time from Rule where 1 = 1")
                database.execSQL("Drop table Rule")
                database.execSQL("Alter table Rule_t rename to Rule")
                database.execSQL("CREATE UNIQUE INDEX \"index_Rule_id\" ON \"Rule\" ( \"id\" ASC)")*/
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table Rule add column sender_logic TEXT NOT NULL DEFAULT 'ALL'")
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //database.execSQL("Create table Msg as Select id,type,`from`,content,(case when sim_info like 'SIM1%' then '0' when sim_info like 'SIM2%' then '1' else '-1' end) as sim_slot,sim_info,sub_id,time from Logs where 1 = 1")
                database.execSQL(
                    """
CREATE TABLE "Msg" (
  "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "type" TEXT NOT NULL DEFAULT 'sms',
  "from" TEXT NOT NULL DEFAULT '',
  "content" TEXT NOT NULL DEFAULT '',
  "sim_slot" INTEGER NOT NULL DEFAULT -1,
  "sim_info" TEXT NOT NULL DEFAULT '',
  "sub_id" INTEGER NOT NULL DEFAULT 0,
  "time" INTEGER NOT NULL
)
""".trimIndent()
                )
                database.execSQL("INSERT INTO Msg (id,type,`from`,content,sim_slot,sim_info,sub_id,time) Select id,type,`from`,content,(case when sim_info like 'SIM1%' then '0' when sim_info like 'SIM2%' then '1' else '-1' end) as sim_slot,sim_info,sub_id,time from Logs where 1 = 1")
                database.execSQL("CREATE UNIQUE INDEX \"index_Msg_id\" ON \"Msg\" ( \"id\" ASC)")
                database.execSQL("ALTER TABLE Logs RENAME TO Logs_old")
                //database.execSQL("Create table Logs_new as Select id,id as msg_id,rule_id,sender_id,forward_status,forward_response,time from Logs where 1 = 1")
                database.execSQL(
                    """
CREATE TABLE "Logs" (
  "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "type" TEXT NOT NULL DEFAULT 'sms',
  "msg_id" INTEGER NOT NULL DEFAULT 0,
  "rule_id" INTEGER NOT NULL DEFAULT 0,
  "sender_id" INTEGER NOT NULL DEFAULT 0,
  "forward_status" INTEGER NOT NULL DEFAULT 1,
  "forward_response" TEXT NOT NULL DEFAULT '',
  "time" INTEGER NOT NULL,
  FOREIGN KEY ("msg_id") REFERENCES "Msg" ("id") ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY ("rule_id") REFERENCES "Rule" ("id") ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY ("sender_id") REFERENCES "Sender" ("id") ON DELETE CASCADE ON UPDATE CASCADE
);
""".trimIndent()
                )
                database.execSQL("INSERT INTO Logs (id,type,msg_id,rule_id,sender_id,forward_status,forward_response,time) SELECT id,type,id as msg_id,rule_id,sender_id,forward_status,forward_response,time FROM Logs_old")
                database.execSQL("DROP TABLE Logs_old")
                database.execSQL("CREATE UNIQUE INDEX \"index_Logs_id\" ON \"Logs\" ( \"id\" ASC)")
                database.execSQL("CREATE INDEX \"index_Logs_msg_id\" ON \"Logs\" ( \"msg_id\" ASC)")
                database.execSQL("CREATE INDEX \"index_Logs_rule_id\" ON \"Logs\" ( \"rule_id\" ASC)")
                database.execSQL("CREATE INDEX \"index_Logs_sender_id\" ON \"Logs\" ( \"sender_id\" ASC)")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE VIEW `LogsDetail` AS SELECT LOGS.id,LOGS.type,LOGS.msg_id,LOGS.rule_id,LOGS.sender_id,LOGS.forward_status,LOGS.forward_response,LOGS.TIME,Rule.filed AS rule_filed,Rule.`check` AS rule_check,Rule.value AS rule_value,Rule.sim_slot AS rule_sim_slot,Sender.type AS sender_type,Sender.NAME AS sender_name FROM LOGS  LEFT JOIN Rule ON LOGS.rule_id = Rule.id LEFT JOIN Sender ON LOGS.sender_id = Sender.id")
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table rule add column silent_period_start INTEGER NOT NULL DEFAULT 0 ")
                database.execSQL("Alter table rule add column silent_period_end INTEGER NOT NULL DEFAULT 0 ")
            }
        }

        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table Msg add column call_type INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
CREATE TABLE "Task" (
  "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "type" INTEGER NOT NULL DEFAULT 1,
  "name" TEXT NOT NULL DEFAULT '',
  "description" TEXT NOT NULL DEFAULT '',
  "conditions" TEXT NOT NULL DEFAULT '',
  "actions" TEXT NOT NULL DEFAULT '',
  "last_exec_time" INTEGER NOT NULL,
  "next_exec_time" INTEGER NOT NULL,
  "status" INTEGER NOT NULL DEFAULT 1
)
""".trimIndent()
                )
            }
        }

        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                var smsTemplate = SettingUtils.smsTemplate
                var ruleColumnCN = "sms_template"
                var ruleColumnTW = "sms_template"
                var senderColumnCN = "json_setting"
                var senderColumnTW = "json_setting"

                for (i in TAG_LIST.indices) {
                    val tagCN = TAG_LIST[i]["zh_CN"].toString()
                    val tagTW = TAG_LIST[i]["zh_TW"].toString()
                    val tagEN = TAG_LIST[i]["en"].toString()
                    smsTemplate = smsTemplate.replace(tagCN, tagEN)
                    ruleColumnCN = "REPLACE($ruleColumnCN, '$tagCN', '$tagEN')"
                    ruleColumnTW = "REPLACE($ruleColumnTW, '$tagTW', '$tagEN')"
                    senderColumnCN = "REPLACE($senderColumnCN, '$tagCN', '$tagEN')"
                    senderColumnTW = "REPLACE($senderColumnTW, '$tagTW', '$tagEN')"
                }

                database.execSQL("UPDATE Rule SET sms_template = $ruleColumnCN WHERE sms_template != ''")
                database.execSQL("UPDATE Rule SET sms_template = $ruleColumnTW WHERE sms_template != ''")

                database.execSQL("UPDATE Sender SET json_setting = $senderColumnCN WHERE type NOT IN (4, 5, 6, 7, 8, 14)")
                database.execSQL("UPDATE Sender SET json_setting = $senderColumnTW WHERE type NOT IN (4, 5, 6, 7, 8, 14)")

                SettingUtils.smsTemplate = smsTemplate
            }
        }

        private val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table rule add column silent_day_of_week TEXT NOT NULL DEFAULT '' ")
            }
        }

        private val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table rule add column title TEXT NOT NULL DEFAULT '' ")
            }
        }

    }

}
