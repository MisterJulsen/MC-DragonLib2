package de.mrjulsen.mcdragonlib.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkProcessor.StreamReceiver;
import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.network.NetworkPacketType;
import de.mrjulsen.mcdragonlib.network.NetworkProcessor;
import de.mrjulsen.mcdragonlib.network.NetworkDirection.C2S;
import de.mrjulsen.mcdragonlib.network.NetworkPacketType.Send;
import de.mrjulsen.mcdragonlib.network.NetworkPacketType.SendAndReceive;
import de.mrjulsen.mcdragonlib.network.NetworkPacketType.Stream;
import de.mrjulsen.mcdragonlib.network.NetworkProcessor.StreamProvider;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.IOUtils;
import net.minecraft.nbt.CompoundTag;

public class NetworkTest {

    public static class TestData extends NetworkPacketData {

        private String txt;

        public TestData(DLStatus status) {
            super(status);
        }

        public TestData(String txt) {
            this(DLStatus.OK);
            this.txt = txt;
        }

        public String txt() {
            return txt;
        }

        @Override
        protected void write(CompoundTag nbt) {
            nbt.putString("txt", txt);
        }

        @Override
        protected void read(CompoundTag nbt) {
            this.txt = nbt.getString("txt");
        }        
    }
    public static class FileData extends NetworkPacketData {

        private byte[] data;

        

        public FileData(DLStatus status) {
            super(status);
        }

        public FileData(String path) {
            this(DLStatus.OK);
            try {
                this.data = IOUtils.inputStreamToByteArray(IOUtils.readFile(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void write(CompoundTag nbt) {
            nbt.putByteArray("txt", data);
        }

        @Override
        public void read(CompoundTag nbt) {
            this.data = nbt.getByteArray("txt");
        }
        
        public String saveFile() {
            try {
                IOUtils.saveInputStreamToFile(IOUtils.byteArrayToInputStream(data), "C:\\Users\\julia\\Desktop\\dometo moderator short.mp4");
                return IOUtils.formatBytes(new File("C:\\Users\\julia\\Desktop\\dometo moderator short.mp4").length());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "?";
        }
    }


    

    public static class FileChunk extends NetworkPacketData {

        private byte[] data;// = new byte[0];

        public FileChunk(DLStatus status) {
            super(status);
        }
        
        public FileChunk(DLStatus status, byte[] data) {
            super(status);
            this.data = data;
        }

        @Override
        protected void write(CompoundTag nbt) {
            nbt.putByteArray("data", data);
        }

        @Override
        protected void read(CompoundTag nbt) {
            this.data = nbt.getByteArray("data");
            if (data == null) {
                //this.data = new byte[0];
            }
        }

        public byte[] getBytes() {
            return data;
        }
        
    }

    public static class FileTransferResponse extends NetworkPacketData {

        public FileTransferResponse(DLStatus status) {
            super(status);
        }

        @Override
        protected void write(CompoundTag nbt) {
        }

        @Override
        protected void read(CompoundTag nbt) {
        }
        
    }

    public static class FileTransferProvider implements StreamProvider<FileChunk, FileTransferResponse> {

        private static final int CHUNK_SIZE = 1024 * 1024; // 1 MB
        private final BufferedInputStream inputStream;
        private boolean done = false;

        public FileTransferProvider(String filename) {
            try {
                File file = new File(filename);
                if (!file.exists() || !file.isFile()) {
                    throw new IllegalArgumentException("Datei nicht gefunden: " + filename);
                }
                this.inputStream = new BufferedInputStream(new FileInputStream(file));
            } catch (IOException e) {
                throw new RuntimeException("Fehler beim Öffnen der Datei: " + filename, e);
            }
        }

        @Override
        public FileChunk execute(Optional<FileTransferResponse> data) {
            if (done) {
                System.out.println("IS DONE");
                return new FileChunk(DLStatus.DONE, new byte[0]);
            }

            try {
                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead = inputStream.read(buffer);

                if (bytesRead == -1) {
                    close();
                    done = true;
                System.out.println("FINISHED");
                    return new FileChunk(DLStatus.DONE, new byte[0]);
                }

                // Falls weniger als 1MB gelesen wurde, Array verkleinern
                if (bytesRead < CHUNK_SIZE) {
                    byte[] actualData = new byte[bytesRead];
                    System.arraycopy(buffer, 0, actualData, 0, bytesRead);
                    return new FileChunk(DLStatus.OK, actualData);
                }

                return new FileChunk(DLStatus.OK, buffer);
            } catch (IOException e) {
                close();
                done = true;
                throw new RuntimeException("Fehler beim Lesen der Datei", e);
            }
        }

        private void close() {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    
    public static class FileReceiver implements StreamReceiver<FileChunk, FileTransferResponse> {

        private static final String FILENAME = "C:\\Users\\julia\\Desktop\\video.mp4"; // TODO: Zielpfad anpassen
        private BufferedOutputStream outputStream;
        private boolean done = false;

        public FileReceiver() {
            try {
                // Datei neu anlegen / überschreiben
                this.outputStream = new BufferedOutputStream(new FileOutputStream(FILENAME, false));
            } catch (IOException e) {
                throw new RuntimeException("Fehler beim Erstellen der Ausgabedatei: " + FILENAME, e);
            }
        }

        @Override
        public FileTransferResponse execute(FileChunk data) {
            if (done) {
                System.out.println("NOTHING TO SAVE");
                // Bereits abgeschlossen – keine weiteren Daten akzeptieren
                return new FileTransferResponse(DLStatus.DONE);
            }

            try {
                if (data.getStatus().isDone()) {
                    // Übertragung abgeschlossen
                    close();
                    done = true;
                    System.out.println("NOTHING TO SAVE 2");
                    return new FileTransferResponse(DLStatus.DONE);
                }

                byte[] bytes = data.getBytes();
                if (bytes != null && bytes.length > 0) {
                    outputStream.write(bytes);
                }

                return new FileTransferResponse(DLStatus.OK);

            } catch (IOException e) {
                close();
                done = true;
                throw new RuntimeException("Fehler beim Schreiben in Datei: " + FILENAME, e);
            }
        }

        private void close() {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException ignored) {
            }
        }
    }





    public static final NetworkPacketType.Send<NetworkDirection.C2S, TestData> SEND = DLNetworkManager.registerSendOnlyPacket(DLUtils.resourceLocation("salz"), NetworkDirection.C2S,
        (data) -> {
            DragonLib.LOGGER.info("Text message is: " + data.txt);
        }, TestData::new);

    public static final NetworkPacketType.SendAndReceive<NetworkDirection.C2S, FileData, TestData> COPY_FILE = DLNetworkManager.registerSendAndReceivePacket(DLUtils.resourceLocation("salz2"), NetworkDirection.C2S,
        (data) -> {
            return new TestData(data.saveFile());
        }, FileData::new, TestData::new);

    
    public static final NetworkPacketType.Stream<NetworkDirection.C2S, FileChunk, FileTransferResponse> FILE_TRANSFER = DLNetworkManager.registerStreamPacket(DLUtils.resourceLocation("file_transfer"), NetworkDirection.C2S,
        FileReceiver::new, FileChunk::new, FileTransferResponse::new);

    public static void init() {
    }

}
