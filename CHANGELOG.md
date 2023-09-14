# 0.2.0

- S3 Upload / Download

# 0.1.2

- File Unicode
- S3 Option

```ini
s3_enabled=${S3_ENABLED:false}
```

# 0.1.1

- `thymeleaf` Option

```ini
# before
spring.thymeleaf.prefix=file:/templates/

# fix
spring.thymeleaf.prefix=classpath:/templates/
```

- return url

```java
// before
return "/dataList.html";

// fix
return "dataList.html";
```

# 0.1.0

- Database Insert
- File encrypt/decrypt