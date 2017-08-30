package log.util.slowsql;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: log.util
 * ClassName: ReadSlowSqlFromLog
 * Author:
 *
 * Description:
 * Date: 13:20 2017/8/22
 * Version: 1.0
 */
public class ReadSlowSqlFromLog {

    public static void main(String[] args) throws IOException,ClassNotFoundException {

        ReadSlowSqlFromLog readSlowSqlFromLog = new ReadSlowSqlFromLog();
        String fileName = "ERROR118.log,ERROR119.log,ERROR166.log,ERROR183.log";
        String[] fileArray = fileName.split(",");

        for(int i = 0 ; i < fileArray.length ; i++ ){
            String inFilePath = "C:\\Users\\fing\\Desktop\\errorlog\\0821\\" + fileArray[i];
            String outFilePath = "C:\\Users\\fing\\Desktop\\errorlog\\0821\\" + fileArray[i].replace("ERROR","slowSQL").replace("log","txt");
            readSlowSqlFromLog.readLogSlowSql(inFilePath,outFilePath);
        }
    }




    /**
     * 读取 slow sql
     * @param inFilePath
     * @param outFilePath
     */
    public void readLogSlowSql(String inFilePath,String outFilePath){
        try {

            BufferedReader reader = new BufferedReader(new FileReader(inFilePath));//创建输入流
            FileOutputStream out = new FileOutputStream(outFilePath);//创建输出流

            int count = 0;

            int sqlMaxTime = 0;
            int sqlMinTime = 0;

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();

                if(line.startsWith("slow sql")){//提取当前的数据的 slow sql 的时间行

                    count++ ;

                    //提取这里边的数据
                    String sqlTime = "";
                    for(int i=0;i<line.length();i++) {
                        if (line.charAt(i) >= 48 && line.charAt(i) <= 57) {
                            sqlTime += line.charAt(i);
                        }
                    }

                    //System.out.println("sql使用时间（ms）：\n" + newStr);
                    out.write(("sql使用时间（ms）：\r\n" + sqlTime).getBytes());

                    if(sqlMaxTime < Integer.valueOf(sqlTime)){
                        sqlMaxTime = Integer.valueOf(sqlTime);
                    }


                    if(sqlMinTime == 0){
                        sqlMinTime = Integer.valueOf(sqlTime);
                    }
                    else{
                        if(sqlMinTime >= Integer.valueOf(sqlTime)){
                            sqlMinTime = Integer.valueOf(sqlTime);
                        }
                    }

                    String sqlLine = reader.readLine(); //sql 行
                    List<String> paramsArray = getArrayByStr(reader.readLine());//获取参数

                    if(paramsArray != null){
                        for(int i = 0;i < paramsArray.size() ; i++){
                            sqlLine = replaceSQLString(sqlLine,"'"+paramsArray.get(i)+"'",1);
                        }
                    }


                    //System.out.println("sql语句：\n"+ sqlLine.replaceAll(" +"," ") +"\n");
                    out.write(("\r\nsql语句：\r\n"+ sqlLine.replaceAll(" +"," ") +"\r\n\r\n\r\n").getBytes());
                }

            }

            out.write(("当前一共有"+ count + "个慢sql！！！").getBytes());
            out.write(("花费最长时间："+ sqlMaxTime + " ms").getBytes());
            out.write(("花费最短时间："+ sqlMinTime + " ms").getBytes());
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("慢sql解析成功！");
    }




    /**
     * ["111","340826199208034047","0",100,0] 转化为 String 数组
     * @param str
     * @return
     */
    public static List<String> getArrayByStr(String str){

        if("".equals(str) || "[]".equals(str) || str == null){
            return null;
        }

        str = str.substring(1,str.length()-1);//去掉 中括号
        String[] strAry = str.split(",");
        List<String> paramsList = new ArrayList<>();
        String temp = "";
        boolean isBegin = false;
        boolean isOver  = false ;
        for(int i = 0; i < strAry.length; i++){

            if(strAry[i].startsWith("\"") && !strAry[i].endsWith("\"")
                    || !strAry[i].startsWith("\"") && strAry[i].endsWith("\"")){

                isBegin = true;
                if(!strAry[i].startsWith("\"") && strAry[i].endsWith("\"")){
                    isOver  = true;
                }
            }
            if(isBegin){//如果是多个
                temp += ","+strAry[i];
                if(isOver){
                    if(temp.startsWith(",")){
                        temp = temp.substring(1);
                    }
                    paramsList.add(temp.replaceAll("\"",""));
                    temp = "";
                    isOver = false;
                    isBegin = false;
                }
            }
            else{//如果是单个
                temp = strAry[i];
                paramsList.add(temp.replaceAll("\"",""));
                temp = "";
            }
        }
        return paramsList;
    }


    /**
     * 替换 sql 中的 ？字符串
     * @param str   被替换字符串
     * @param rstr  提出按的每个字符串
     * @param replaceCount     替换的位置
     * @return
     */
    public static String replaceSQLString(String str, String rstr, int replaceCount) {

        String searchStr = "?";
        int index = str.indexOf(searchStr);

        int count = 1;
        while (count != replaceCount) {
            index = str.indexOf(searchStr, index + 1);
            count++;
        }

        return str.substring(0, index) + rstr + str.substring(index + 1);
    }

}
