package grape.code.fill;

import java.io.*;

/**
 * Created by yangwei
 * Created at 2019/7/29 11:46
 */
public class DebugTool {

    private static int count = 1;
    private static String filePath = "d:/debug-code-fill/";
    public static void info(String info) {

        File file = null;
        try {
            file = createFile(filePath + count++ + info,false);
            /*String content = getFileContent(file);
            String newContent = content + "\t\n" + info;
            writeString(file,newContent,"UTF-8");*/
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static File getFile(String filePath){
        File file = null;
        file = new File(filePath);
        return file;
    }
    private static File createFile(String filePath,boolean flag) throws IOException {
        File file = null;
        file = getFile(filePath);
        if(!file.exists()){
            file.createNewFile();
        }else{
            //如果存在重名文件
            if(flag){
                //如果要删除重名文件
                file.delete();
                file.createNewFile();
            }
        }
        return file;
    }
    /**
     * 向文件中写入字符串
     * @param file
     * @param string
     * @throws IOException
     */
    private static void writeString(File file,String string,String encode){
        OutputStreamWriter write = null;
        BufferedWriter writer = null;
        try {
            write = new OutputStreamWriter(new FileOutputStream(file), encode);
            writer = new BufferedWriter(write);
            writer.write(string);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("writeString to file " + file.getPath(),e);
        }finally {
            if(writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
            if(write != null){
                try {
                    write.close();
                } catch (IOException e) {
                }
            }
        }
    }
    /**
     * 一次读取文件所有内容
     * @param file
     * @return
     * @throws IOException
     */
    private static String getFileContent(File file) throws IOException {
        Long fileLengthLong = file.length();
        byte[] fileContent = new byte[fileLengthLong.intValue()];
        FileInputStream inputStream = new FileInputStream(file);
        inputStream.read(fileContent);
        inputStream.close();
        String string = new String(fileContent);
        return string;
    }
}
