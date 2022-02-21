package tech.eboot.xplanet.storage.local;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

@Slf4j
public class MappedFile {
    public static final int OS_PAGE_SIZE = 1024 * 4;
    
    private String fileName;
    private int fileSize;
    private File file;
    private FileChannel fileChannel;


    private void init(final String fileName, final int fileSize) throws IOException
    {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.file = new File(fileName);
        //this.fileFromOffset = Long.parseLong(this.file.getName());
        boolean ok = false;

        //ensureDirOK(this.file.getParent());

        try {
            this.fileChannel = new RandomAccessFile(this.file, "rw").getChannel();
            //this.mappedByteBuffer = this.fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
            //TOTAL_MAPPED_VIRTUAL_MEMORY.addAndGet(fileSize);
            //TOTAL_MAPPED_FILES.incrementAndGet();
            ok = true;
        } catch (FileNotFoundException e) {
            log.error("Failed to create file " + this.fileName, e);
            throw e;
        } catch (IOException e) {
            log.error("Failed to map file " + this.fileName, e);
            throw e;
        } finally {
            if (!ok && this.fileChannel != null) {
                this.fileChannel.close();
            }
        }
    }
}
