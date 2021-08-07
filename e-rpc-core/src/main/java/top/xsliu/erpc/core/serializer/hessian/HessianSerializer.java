package top.xsliu.erpc.core.serializer.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import top.xsliu.erpc.core.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * hessian2序列化
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/8/1:19 上午
 * @author: lxs
 */
@Slf4j
public class HessianSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Hessian2Output ho = new Hessian2Output(os);
        try {
            ho.writeObject(obj);
            ho.flush();
            return os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                ho.close();
            } catch (IOException e) {
                log.error("something goes wrong " + e.getMessage());
            }
            try {
                os.close();
            } catch (IOException e) {
                log.error("something goes wrong " + e.getMessage());
            }
        }

    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        Hessian2Input hi = new Hessian2Input(is);
        try {
            return hi.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                hi.close();
            } catch (Exception e) {
                log.error("something goes wrong " + e.getMessage());
            }
            try {
                is.close();
            } catch (IOException e) {
                log.error("something goes wrong " + e.getMessage());
            }
        }
    }

}
