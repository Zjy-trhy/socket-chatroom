package iotest;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class CopyMain {

    private static final int ROUNDS = 5;

    public static void main(String[] args) {
        FileCopyRunner noBufferStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                InputStream fin = null;
                OutputStream fout = null;
                try {
                    fin = new FileInputStream(source);
                    fout = new FileOutputStream(target);
                    int result;
                    while ((result = fin.read()) != -1) {
                        fout.write(result);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(fin);
                    close(fout);
                }
            }

            @Override
            public String toString() {
                return "noBufferStreamCopy";
            }
        };

        FileCopyRunner bufferedStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                InputStream fin = null;
                OutputStream fout = null;
                try {
                    fin = new BufferedInputStream(new FileInputStream(source));
                    fout = new BufferedOutputStream(new FileOutputStream(target));
                    //自定义缓冲区
                    byte[] buffer = new byte[1024];
                    int result;
                    while ((result = fin.read(buffer)) != -1) {
                        fout.write(buffer, 0, result);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(fin);
                    close(fout);
                }
            }

            @Override
            public String toString() {
                return "bufferedStreamCopy";
            }
        };

        FileCopyRunner nioBufferCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                FileChannel fin = null;
                FileChannel fout = null;
                try {
                    fin = new FileInputStream(source).getChannel();
                    fout = new FileOutputStream(target).getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    while ((fin.read(buffer)) != -1) {
                        //切换到读出模式，buffer
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            fout.write(buffer);
                        }
                        //将buffer切换到写入模式
                        buffer.clear();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(fin);
                    close(fout);
                }
            }

            @Override
            public String toString() {
                return "nioBufferCopy";
            }
        };

        FileCopyRunner nioTransferCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                FileChannel fin = null;
                FileChannel fout = null;
                try {
                    fin = new FileInputStream(source).getChannel();
                    fin = new FileOutputStream(source).getChannel();
                    long transferred = 0L;
                    long size = fin.size();
                    while (transferred != size) {
                        transferred += fin.transferTo(0, size, fout);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(fin);
                    close(fout);
                }
            }

            @Override
            public String toString() {
                return "nioTransferCopy";
            }
        };

        File smallFile = new File("D:\\IdeaProjects\\socket\\BIO\\src\\main\\resources\\samll.docx");
        File smallFileCopy = new File("D:\\IdeaProjects\\socket\\BIO\\src\\main\\resources\\smallCopy.docx");

        System.out.println("---Copying small file---");
        benchMark(noBufferStreamCopy, smallFile, smallFileCopy);
        benchMark(bufferedStreamCopy, smallFile, smallFileCopy);
        benchMark(nioBufferCopy, smallFile, smallFileCopy);
        benchMark(nioTransferCopy, smallFile, smallFileCopy);

        File bigFile = new File("D:\\IdeaProjects\\socket\\BIO\\src\\main\\resources\\big.pdf");
        File BigFileCopy = new File("D:\\IdeaProjects\\socket\\BIO\\src\\main\\resources\\bigCopy.pdf");

        System.out.println("---Copying big file---");
//        benchMark(noBufferStreamCopy, bigFile, BigFileCopy);
        benchMark(bufferedStreamCopy, bigFile, BigFileCopy);
        benchMark(nioBufferCopy, bigFile, BigFileCopy);
        benchMark(nioTransferCopy, bigFile, BigFileCopy);

        File hugeFile = new File("D:\\IdeaProjects\\socket\\BIO\\src\\main\\resources\\huge.flv");
        File hugeFileCopy = new File("D:\\IdeaProjects\\socket\\BIO\\src\\main\\resources\\hugeCopy.pdf");

        System.out.println("---Copying huge file---");
//        benchMark(noBufferStreamCopy, hugeFile, hugeFileCopy);
        benchMark(bufferedStreamCopy, hugeFile, hugeFileCopy);
        benchMark(nioBufferCopy, hugeFile, hugeFileCopy);
        benchMark(nioTransferCopy, hugeFile, hugeFileCopy);
    }

    public static void benchMark(FileCopyRunner test, File source, File target) {
        long elapsed = 0L;
        for (int i = 0; i < ROUNDS; i++) {
            long startTime = System.currentTimeMillis();
            test.copyFile(source, target);
            elapsed += System.currentTimeMillis() - startTime;
            target.delete();
        }
        System.out.println(test + ": " + elapsed / ROUNDS);
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
