package de.mrjulsen.mcdragonlib.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dev.architectury.platform.Platform;

/**
 * Small collection of file and stream helper utilities used by the mod.
 *
 * <p>Convenience wrappers handle reading/writing files, converting streams to byte arrays,
 * creating config directories, and computing a simple SHA-256 based file hash.
 */
public final class IOUtils {

    /**
     * Open a file for reading.
     *
     * @param filePath path to the file
     * @return InputStream for the file
     * @throws IOException when file cannot be opened
     */
    public static InputStream readFile(String filePath) throws IOException {
        File file = new File(filePath);
        return new FileInputStream(file);
    }

    /**
     * Save an InputStream to a file path. Caller is responsible for providing a valid stream.
     *
     * @param inputStream source stream
     * @param filePath destination file path (created/overwritten)
     * @throws IOException on I/O error
     */
    public static void saveInputStreamToFile(InputStream inputStream, String filePath) throws IOException {
        File file = new File(filePath);
        OutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();
    }

    /**
     * Write text content to a file (UTF-8).
     *
     * @param filePath destination path
     * @param content text content
     * @throws IOException on failure
     */
    public static void writeTextFile(String filePath, String content) throws IOException {
        Files.writeString(Path.of(filePath), content);
    }

    /**
     * Read a text file and return its contents as UTF-8 string.
     *
     * @param filePath path to file
     * @return file contents
     * @throws IOException on failure
     */
    public static String readTextFile(String filePath) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Path.of(filePath));
        return new String(fileBytes, StandardCharsets.UTF_8);
    }

    /**
     * Wrap a byte array into an InputStream.
     *
     * @param byteArray source bytes
     * @return InputStream over the bytes
     */
    public static InputStream byteArrayToInputStream(byte[] byteArray) {
        return new ByteArrayInputStream(byteArray);
    }

    /**
     * Read all bytes from an InputStream into a byte array.
     *
     * @param inputStream stream to read (not closed by this method)
     * @return byte[] with stream data
     * @throws IOException on read error
     */
    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }

    /**
     * Create a directory (and parents) if it does not exist.
     *
     * @param directoryPath path to create
     * @return true if directory was created, false if it already existed
     */
    public static boolean createDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            return created;
        } else {
            return false;
        }
    }

    /**
     * List file names (not directories) inside a directory.
     *
     * @param directoryPath path to directory
     * @return array of file names (empty if none or on error)
     */
    public static String[] getFileNames(String directoryPath) {
        List<String> fileNames = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {
            for (Path path : directoryStream) {
                if (Files.isRegularFile(path)) {
                    fileNames.add(path.getFileName().toString());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return fileNames.toArray(new String[0]);
    }

    /**
     * Human-readable byte size formatter (KB/MB/...).
     *
     * @param bytes number of bytes
     * @return formatted string like "1.2 MB"
     */
    public static String formatBytes(long bytes) {
        int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (java.lang.Math.log(bytes) / java.lang.Math.log(unit));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / java.lang.Math.pow(unit, exp), pre);
    }

    /**
     * Return file names without extensions in a directory.
     *
     * @param directoryPath directory to scan
     * @return array of file names without extension
     */
    public static String[] getFileNamesWithoutExtension(String directoryPath) {
        List<String> fileNames = new ArrayList<>();

        File directory = new File(directoryPath);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        int dotIndex = fileName.lastIndexOf(".");
                        if (dotIndex != -1) {
                            String fileNameWithoutExtension = fileName.substring(0, dotIndex);
                            fileNames.add(fileNameWithoutExtension);
                        }
                    }
                }
            }
        }

        return fileNames.toArray(new String[0]);
    }

    /**
     * Extract the file name (without path) from a full path.
     *
     * @param filePath path to file
     * @return file name
     */
    public static String getFileNameWithoutExtension(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    /**
     * Return the extension of a file name (without dot). Returns empty string if no extension.
     *
     * @param fileName file name or path
     * @return file extension or "" or null when input null
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }

        int lastDotIndex = fileName.lastIndexOf(".");

        if (lastDotIndex == -1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * Validate file name characters for common OSes.
     *
     * @param fileName file name (no path)
     * @return true if file name contains no illegal characters
     */
    public static boolean isValidFileName(String fileName) {
        String invalidCharsRegex = "[\\\\/:*?\"<>|]";
        Pattern pattern = Pattern.compile(invalidCharsRegex);
        return !pattern.matcher(fileName).find();
    }

    /**
     * Remove characters illegal for file names.
     *
     * @param fileName input file name
     * @return sanitized file name
     */
    public static String sanitizeFileName(String fileName) {
        String invalidCharsRegex = "[\\\\/:*?\"<>|]";
        String sanitizedFileName = fileName.replaceAll(invalidCharsRegex, "");
        return sanitizedFileName;
    }

    /**
     * Compute a simple SHA-256 based hash from file metadata (name+size+modified).
     *
     * @param filePath path to the file
     * @return hex string of hash or null if algorithm unavailable
     */
    public static String getFileHash(String filePath) {        
        try {
            File file = new File(filePath);
            String data = file.getName() + file.length() + file.lastModified();
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");            
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getConfigDirForMod(String modid) {
        return String.format("%s/%s", Platform.getConfigFolder(), modid);
    }

    /**
     * Create the config directory for a mod and return its path.
     *
     * @param modid mod id
     * @return path to config directory
     */
    public static String createConfigDirectory(String modid) {
        String path = getConfigDirForMod(modid);
        createDirectory(path);
        return path;
    }

    /**
     * Write a text config file inside mod's config directory.
     *
     * @param modid mod id
     * @param relFilePath relative path inside config dir
     * @param content text content to write
     * @throws IOException on error
     */
    public static void writeConfigTextFile(String modid, String relFilePath, String content) throws IOException {
        String path = getConfigDirForMod(modid) + "/" + relFilePath;
        createDirectory(path);
        Files.writeString(Path.of(path), content);
    }

    /**
     * Read a text config file from mod's config directory.
     *
     * @param modid mod id
     * @param relFilePath relative path inside config dir
     * @return file contents
     * @throws IOException on error
     */
    public static String readConfigTextFile(String modid, String relFilePath) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Path.of(getConfigDirForMod(modid) + "/" + relFilePath));
        return new String(fileBytes, StandardCharsets.UTF_8);
    }
}


