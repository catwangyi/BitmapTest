import java.awt.*;
import java.io.*;
import java.util.concurrent.ForkJoinPool;

/**
 * @author wang
 * @create 2019-04-09 16:25
 * @desc
 **/
public class BitmapTest {
    static final int BITMAPFILEHEADER=14;//说明文件的类型和位图数据的起始位置等，共14个字节。
    static final int BITMAPINFORMATION=40;//说明位图文件的大小，位图的高度、宽度、位图的颜色格式和压缩类型等信息。共40个字节。
    static int width;//位图的宽
    static int height;//位图的高
    static int[][] red,green,blue;
    static final String imgpath="test.bmp";
    byte[] bitmapFileHeader;
    byte[] bitmapInformation;
    byte[] newBitmapInformation;
    Graphics g;
    public static void main(String[] args) {
        BitmapTest BitmapTest =new BitmapTest();
        BitmapTest.init();
    }
    public void init(){
        try {
            FileInputStream file = new FileInputStream(imgpath);
            DataInputStream dis=new DataInputStream(file);
            //位图文件头
            bitmapFileHeader=new byte[14];
            dis.read(bitmapFileHeader,0,BITMAPFILEHEADER);
            /*for (int i=0;i<bitmapFileHeader.length;i++){
                System.out.println("bitmapFileHeader"+Integer.toHexString(bitmapFileHeader[i]));
            }*/
            //位图信息头
            bitmapInformation=new byte[40];
            dis.read(bitmapInformation,0,BITMAPINFORMATION);

            /*for (byte b:bitmapInformation){
                System.out.println("b:"+Integer.toHexString(b));
            }
            System.out.println("+++++++++++++++++");*/


            //得到bmp文件的数据，将byte型转化为int型
            //得到宽和高
            width=converInt(bitmapInformation,7);
            height=converInt(bitmapInformation,11);

            int nsizeimage=converInt(bitmapFileHeader,5);
            int skip_width=0;
            if(width*3 % 4!=0){//后面有补0 的情况
                skip_width =4-width*3%4;
            }
            System.out.println("源图大小:"+nsizeimage);
            System.out.println("width:"+width);
            System.out.println("height:"+height);

            red=new int[height][width];
            green=new int[height][width];
            blue=new int[height][width];

            //将bmp位图数据读取成byte数组;
            for (int i=0;i<height;i++){
                for (int j=0;j<width;j++){
                    try {
                        blue[i][j]=dis.readByte();
                        green[i][j]=dis.readByte();
                        red[i][j]=dis.readByte();
                        if ((red[i][j]+green[i][j]+blue[i][j])>=0){
                            red[i][j]=0;//黑色
                            green[i][j]=0;
                            blue[i][j]=0;
                        }else{
                            red[i][j]=-1;//白色
                            green[i][j]=-1;
                            blue[i][j]=-1;
                        }
                       /* System.out.println("blue[i][j]"+blue[i][j]);
                        System.out.println("green[i][j]"+green[i][j]);
                        System.out.println("red[i][j]"+red[i][j]);*/
                        if(j==width-1){//跳过补填充项
                            dis.skipBytes(skip_width);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            save();
            Rotate_Reverse();
            Rotate_Left();
            Rotate_Right();

            dis.close();
            file.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public int converInt(byte[] array,int start){
        int i=(((array[start]&0xff)<<24)
                |((array[start-1]&0xff)<<16)
                |((array[start-2]&0xff)<<8)
                |(array[start-3]&0xff));
        return i;
    }
    public byte[] convertByte(int data){
        byte b1 = (byte)(data>>24);
        byte b2 = (byte)((data>>16));
        byte b3 = (byte)((data>>8));
        byte b4 = (byte)((data));
        byte[] bytes={b1,b2,b3,b4};
        return bytes;
    }

    public void save(){
        try {
            FileOutputStream fos=new FileOutputStream("untitled.bmp");
            DataOutputStream dos=new DataOutputStream(fos);
            int skip_width=0;
            if(width*3 % 4!=0){//后面有补0 的情况
                skip_width =4-width*3%4;
            }
            //输入文件头数据
            dos.write(bitmapFileHeader);

            //输入信息头数据
            dos.write(bitmapInformation);
            for (int i=0;i<height;i++){
                for (int j=0;j<width;j++){
                    dos.write(blue[i][j]);
                    dos.write(green[i][j]);
                    dos.write(red[i][j]);
                    if (j==width-1){
                        for (int p=skip_width-1;p>=0;p--){//补充字节
                            dos.writeByte(0);
                        }
                    }

                }
            }
            dos.flush();
            dos.close();
            fos.close();
            System.out.println("保存成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Rotate_Left(){//左旋
        try {
            int newWidth=height;
            int newHeight=width;

            FileOutputStream fos=new FileOutputStream("untitled_left.bmp");
            DataOutputStream dos=new DataOutputStream(fos);
            int skip_width=0;
            if(newWidth*3 % 4!=0){//后面有补0 的情况
                skip_width =4-newWidth*3%4;
            }
            //输入文件头数据
            dos.write(bitmapFileHeader);

            newBitmapInformation=bitmapInformation;
            for (int i=4;i<8;i++){
                newBitmapInformation[i]=convertByte(height)[7-i];
            }
            for (int i=8;i<12;i++){
                newBitmapInformation[i]=convertByte(width)[11-i];
            }

            /*for (byte b:newBitmapInformation){
                System.out.println("b:"+Integer.toHexString(b));
            }*/
            //输入信息头数据
            dos.write(newBitmapInformation);
            for (int j=newHeight-1;j>=0;j--){
                for (int i=0;i<newWidth;i++){
                    dos.write(blue[i][j]);
                    dos.write(green[i][j]);
                    dos.write(red[i][j]);

                    if (i==newWidth-1){
                        for (int p=skip_width-1;p>=0;p--){
                            dos.writeByte(0);
                        }
                    }
                }
            }

            dos.flush();
            dos.close();
            fos.close();
            System.out.println("左旋完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Rotate_Right(){//右旋
        try {
            int newWidth=height;
            int newHeight=width;

            FileOutputStream fos=new FileOutputStream("untitled_right.bmp");
            DataOutputStream dos=new DataOutputStream(fos);
            int skip_width=0;
            if(newWidth*3 % 4!=0){//后面有补0 的情况
                skip_width =4-newWidth*3%4;
            }
            //输入文件头数据
            dos.write(bitmapFileHeader);

            newBitmapInformation=bitmapInformation;
            for (int i=4;i<8;i++){
                newBitmapInformation[i]=convertByte(height)[7-i];
            }
            for (int i=8;i<12;i++){
                newBitmapInformation[i]=convertByte(width)[11-i];
            }

            /*for (byte b:newBitmapInformation){
                System.out.println("b:"+Integer.toHexString(b));
            }*/
            //输入信息头数据
            dos.write(newBitmapInformation);
            for (int j=0;j<newHeight;j++){
                for (int i=newWidth-1;i>=0;i--){
                    dos.write(blue[i][j]);
                    dos.write(green[i][j]);
                    dos.write(red[i][j]);

                    if (i==0){
                        for (int p=skip_width-1;p>=0;p--){
                            dos.writeByte(0);
                        }
                    }
                }
            }

            dos.flush();
            dos.close();
            fos.close();
            System.out.println("右旋完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Rotate_Reverse(){//180°旋转
        try {

            FileOutputStream fos=new FileOutputStream("untitled_reverse.bmp");
            DataOutputStream dos=new DataOutputStream(fos);
            int skip_width=0;
            if(width*3 % 4!=0){//后面有补0 的情况
                skip_width =4-width*3%4;
            }
            //输入文件头数据
            dos.write(bitmapFileHeader);

            /*for (byte b:newBitmapInformation){
                System.out.println("b:"+Integer.toHexString(b));
            }*/
            //输入信息头数据
            dos.write(bitmapInformation);
            for (int i=height-1;i>=0;i--){
                for (int j=width-1;j>=0;j--){
                    /*if ((red[i][j]+green[i][j]+blue[i][j])>=0){
                        red[i][j]=0;//黑色
                        green[i][j]=0;
                        blue[i][j]=0;
                    }else{
                        red[i][j]=-1;//白色
                        green[i][j]=-1;
                        blue[i][j]=-1;
                    }*/
                    dos.write(blue[i][j]);
                    dos.write(green[i][j]);
                    dos.write(red[i][j]);

                    if (j==0){
                        for (int p=skip_width-1;p>=0;p--){
                            dos.writeByte(0);
                        }
                    }
                }
            }

            dos.flush();
            dos.close();
            fos.close();
            System.out.println("180°旋转完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
