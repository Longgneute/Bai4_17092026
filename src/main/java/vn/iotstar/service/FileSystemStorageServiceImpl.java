package vn.iotstar.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import vn.iotstar.config.StorageProperties;
import vn.iotstar.exception.StorageException;

@Service
public class FileSystemStorageServiceImpl implements IStorageService {

    private final Path rootLocation;

    public FileSystemStorageServiceImpl(StorageProperties properties) {

        this.rootLocation = Paths.get(properties.getLocation())
                .toAbsolutePath()
                .normalize();

        // Tự động tạo thư mục uploads khi Spring Boot chạy
        try {
            Files.createDirectories(this.rootLocation);

            System.out.println("====================================");
            System.out.println("Storage location: " + this.rootLocation);
            System.out.println("====================================");

        } catch (Exception e) {
            throw new StorageException(
                    "Không thể tạo thư mục lưu file: " + this.rootLocation, e);
        }
    }

    @Override
    public String getSorageFilename(MultipartFile file, String id) {

        String originalFilename = file.getOriginalFilename();

        String ext = FilenameUtils.getExtension(originalFilename);

        if (ext == null || ext.isEmpty()) {
            ext = "jpg";
        }

        return "p" + id + "." + ext;
    }

    @Override
    public void store(MultipartFile file, String storeFilename) {

        try {

            if (file == null || file.isEmpty()) {
                throw new StorageException("File upload đang rỗng");
            }

            // Đảm bảo thư mục tồn tại
            Files.createDirectories(rootLocation);

            Path destinationFile = rootLocation
                    .resolve(Paths.get(storeFilename))
                    .normalize()
                    .toAbsolutePath();

            // Không cho phép lưu file ra ngoài uploads
            if (!destinationFile.getParent().equals(rootLocation)) {
                throw new StorageException(
                        "Không thể lưu file bên ngoài thư mục uploads");
            }

            try (InputStream inputStream = file.getInputStream()) {

                Files.copy(
                        inputStream,
                        destinationFile,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            System.out.println("Đã lưu file: " + destinationFile);

        } catch (Exception e) {

            e.printStackTrace();

            throw new StorageException(
                    "Failed to store file: " + storeFilename, e);
        }
    }

    @Override
    public Resource loadAsResource(String filename) {

        try {

            Path file = load(filename);

            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }

            throw new StorageException(
                    "Không thể đọc file: " + filename);

        } catch (Exception e) {

            throw new StorageException(
                    "Không thể đọc file: " + filename, e);
        }
    }

    @Override
    public Path load(String filename) {

        return rootLocation
                .resolve(filename)
                .normalize();
    }

    @Override
    public void delete(String storeFilename) throws Exception {

        Path destinationFile = rootLocation
                .resolve(Paths.get(storeFilename))
                .normalize()
                .toAbsolutePath();

        if (Files.exists(destinationFile)) {
            Files.delete(destinationFile);
        }
    }

    @Override
    public void init() {

        try {

            Files.createDirectories(rootLocation);

            System.out.println(
                    "Storage location: "
                    + rootLocation.toAbsolutePath()
            );

        } catch (Exception e) {

            throw new StorageException(
                    "Could not initialize storage", e);
        }
    }
}