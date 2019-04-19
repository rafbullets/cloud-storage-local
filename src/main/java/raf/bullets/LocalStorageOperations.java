package raf.bullets;

import specification.model.FileWrapper;
import specification.model.FolderResult;
import specification.model.FolderWrapper;
import specification.operations.CommonOperations;
import specification.operations.file.FileArchiveOperations;
import specification.operations.file.FileBasicOperations;
import specification.operations.folder.FolderBasicOperations;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LocalStorageOperations implements specification.storage.StorageOperations, CommonOperations, FolderBasicOperations, FileArchiveOperations, FileBasicOperations {
    private String STORAGE_FOLDER;
    private List<String> forbiddenExtensions;

    public void init(String pathToRoot, List<String> forbiddenExtensions) {
        STORAGE_FOLDER = pathToRoot;
        forbiddenExtensions = forbiddenExtensions;
    }

    public void delete(String location, String name) throws Exception {
        File file = new File(STORAGE_FOLDER+File.separator+location+File.separator+name);

        if(!file.delete()) {
            throw new Exception("Can not find the file on the given path.");
        }
    }

    public void createFolder(String location, String name) throws Exception {
        new File(STORAGE_FOLDER+File.separator+location+File.separator+name).mkdirs();
    }

    public void createFolder(String location, String name, String metadata) throws Exception {
        this.createFolder(location, name);

        Path path = Paths.get(STORAGE_FOLDER+File.separator+location+File.separator+"._"+name);
        PrintWriter out = new PrintWriter(path.toFile());
        out.println(metadata);
        out.close();
    }

    public FolderWrapper listFolder(String location, String folderName) throws Exception {
        //If this pathname does not denote a directory, then listFiles() returns null.
        File[] files = new File(STORAGE_FOLDER+File.separator+location+File.separator+folderName).listFiles();

        ArrayList<FolderResult> folderResults = new ArrayList<>(files.length);

        for (File file : files) {
            if(!file.getName().startsWith("._")) {
                folderResults.add(new FolderResult(file.getName(), file.isDirectory()));
            }
        }

        String metadata = "";

        Path path = Paths.get(STORAGE_FOLDER+location+File.separator+"._"+folderName);
        if(Files.exists(path)) {
            FileInputStream fileInputStream = new FileInputStream(path.toFile());
            metadata = this.inputStreamToString(fileInputStream);
        }

        return new FolderWrapper(folderResults, metadata);

    }

    public void uploadAsZipFile(List<File> list, String s, String s1, String s2) throws IOException, Exception {

    }

    private boolean invalidExtension(String fileName)
    {
        return this.forbiddenExtensions.contains(fileName.substring(fileName.lastIndexOf(".")+ 1));
    }

    public void uploadFile(File file, String location, String name) throws IOException, Exception {
        if(invalidExtension(file.getName())) {
            System.out.println("File contains illegal extension");
            return;
        }
        new File(STORAGE_FOLDER+location+File.separator).mkdirs();
        file.renameTo(new File(STORAGE_FOLDER+File.separator+location+File.separator+name));
    }

    public void uploadFile(FileWrapper fileWrapper) throws IOException, Exception {
        if(invalidExtension(fileWrapper.getName())) {
            System.out.println("File contains illegal extension");
            return;
        }
        new File(STORAGE_FOLDER+fileWrapper.getPath()+File.separator).mkdirs();
        fileWrapper.getFile().renameTo(new File(STORAGE_FOLDER+File.separator+fileWrapper.getPath()+File.separator+fileWrapper.getName()));
    }

    public void uploadMultipleFiles(List<FileWrapper> list) throws IOException, Exception {
        for (FileWrapper fileWrapper : list) {
            uploadFile(fileWrapper);
        }
    }

    private String inputStreamToString(InputStream inputStream) throws IOException {
        try(ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toString();
        }
    }

    public FileWrapper downloadFile(String location, String name, String pathOnDisk) throws IOException, Exception {
        String metadata = "";

        Path path = Paths.get(location+File.separator+".__"+name);
        if(Files.exists(path)) {
            FileInputStream fileInputStream = new FileInputStream(path.toFile());
            metadata = this.inputStreamToString(fileInputStream);
        }

        return new FileWrapper(new File(location+name), metadata, name, location);
    }


}
