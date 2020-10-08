package wiki.lanting.common;

import lombok.Data;

@Data
public class LantingResponse<T> {
    public String status = "success";
    public String code = "";
    public T data;

    public LantingResponse<T> success() {
        this.status = "success";
        return this;
    }

    public LantingResponse<T> fail() {
        this.status = "fail";
        return this;
    }

    public LantingResponse<T> error() {
        this.status = "error";
        return this;
    }

    public LantingResponse<T> code(String code) {
        this.code = code;
        return this;
    }

    public LantingResponse<T> data(T data) {
        this.data = data;
        return this;
    }
}
