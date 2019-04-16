import java.io.*;

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
    static int depth;//位深
    static byte[][] red,green,blue;
    static byte[][] pixelInfo;//8位256色
    byte[] color;
    static final String imgpath="origin.bmp";
    byte[] bitmapFileHeader;
    byte[] bitmapInformation;
    byte[] newBitmapInformation;
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
            width=converInt(bitmapInformation,7);//4到7
            height=converInt(bitmapInformation,11);//8到11
            depth=convetIntOfDeepth(bitmapInformation,15);//14到15


            int nsizeimage=converInt(bitmapFileHeader,5);//原图大小
            int skip_width=0;
            if(width*3 % 4!=0){//后面有补0 的情况
                skip_width =4-width*3%4;
            }
            System.out.println("源图大小:"+nsizeimage);
            System.out.println("width:"+width);
            System.out.println("height:"+height);
            System.out.println("位深："+depth);



            if (depth==24){//真彩色，直接描述像素

                red=new byte[height][width];
                green=new byte[height][width];
                blue=new byte[height][width];


                //将bmp位图数据读取成byte数组;
                for (int i=0;i<height;i++){
                    for (int j=0;j<width;j++){
                        try {
                            blue[i][j]=dis.readByte();
                            green[i][j]=dis.readByte();
                            red[i][j]=dis.readByte();

                        /*if ((Byte.toUnsignedInt(red[i][j])+Byte.toUnsignedInt(green[i][j])+Byte.toUnsignedInt(blue[i][j]))>127*3){
                            red[i][j]=(byte)(Integer.parseInt("FF",16));//黑色255
                            green[i][j]=(byte)(Integer.parseInt("FF",16));
                            blue[i][j]=(byte)(Integer.parseInt("FF",16));
                        }else{
                            red[i][j]=Byte.parseByte("00",16);//白色0
                            green[i][j]=Byte.parseByte("00",16);
                            blue[i][j]=Byte.parseByte("00",16);
                        }*/
                        /*System.out.println("red[i][j]"+Byte.toUnsignedInt(red[i][j]));
                        System.out.println("green[i][j]"+Byte.toUnsignedInt(green[i][j]));
                        System.out.println("blue[i][j]"+Byte.toUnsignedInt(blue[i][j]));*/
                            if(j==width-1){//跳过补填充项
                                dis.skipBytes(skip_width);

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }else if (depth==8){
                //由于是256色，所以颜色表是[256][4]大小的数组，每个分量占1个字节
                color = new byte[1024];

                dis.read(color,0,color.length);//调色板

                pixelInfo = new byte[height][width];

                //将bmp位图数据读取成byte数组;
                for (int i=0;i<height;i++){
                    for (int j=0;j<width;j++){
                        try {
                            pixelInfo[i][j]=dis.readByte();
                            /*if(Byte.toUnsignedInt(pixelInfo[i][j])>=127){
                                pixelInfo[i][j]=(byte)(Integer.parseInt("FF",16));//黑色255
                            }else {
                                pixelInfo[i][j]=Byte.parseByte("00",16);//白色0
                            }*/
                            if(j==width-1){//跳过补填充项
                                dis.skipBytes(skip_width);
//                                System.out.println("输出！");

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
        /*int i = (((array[start]&0xff)<<24)
                |((array[start-1]&0xff)<<16)
                |((array[start-2]&0xff)<<8)
                |(array[start-3]&0xff));
        return i;*/
        int i =Byte.toUnsignedInt(array[start])*16777216//16^6
                +Byte.toUnsignedInt(array[start-1])*65536//16^4
                +Byte.toUnsignedInt(array[start-2])*256//16^2
                +Byte.toUnsignedInt(array[start-3]);//16^0
        return i;
    }

    public int convetIntOfDeepth(byte[] array,int start){
        int i =Byte.toUnsignedInt(array[start])*8
                +Byte.toUnsignedInt(array[start-1]);
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

            if (depth==24){
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
            }
            else if (depth==8){
                dos.write(color);
                for (int i=0;i<height;i++){
                    for (int j=0;j<width;j++){
                        dos.write(pixelInfo[i][j]);
                        if (j==width-1){
                            for (int p=0;p<skip_width;p++){//补充字节
                                dos.writeByte(0);
                            }
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

            if (depth==24){
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
            }else if (depth==8){
                dos.write(color);
                for (int j=newHeight-1;j>=0;j--){
                    for (int i=0;i<newWidth;i++){
                        dos.write(pixelInfo[i][j]);
                        if (i==newWidth-1){
                            for (int p=skip_width-1;p>=0;p--){
                                dos.writeByte(0);
                            }
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

            if (depth==24){
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
            }else if (depth==8){
                dos.write(color);
                for (int j=0;j<newHeight;j++){
                    for (int i=newWidth-1;i>=0;i--){
                        dos.write(pixelInfo[i][j]);
                        if (i==0){
                            for (int p=skip_width-1;p>=0;p--){
                                dos.writeByte(0);
                            }
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


            if (depth==24){
                for (int i=height-1;i>=0;i--){
                    for (int j=width-1;j>=0;j--){
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
            }else if (depth==8){
                dos.write(color);
                for (int i=height-1;i>=0;i--){
                    for (int j=width-1;j>=0;j--){
                        dos.write(pixelInfo[i][j]);
                        if (j==0){
                            for (int p=skip_width-1;p>=0;p--){
                                dos.writeByte(0);
                            }
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
