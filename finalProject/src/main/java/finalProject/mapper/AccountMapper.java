package finalProject.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper {
    void pointCharge(Integer point);
}