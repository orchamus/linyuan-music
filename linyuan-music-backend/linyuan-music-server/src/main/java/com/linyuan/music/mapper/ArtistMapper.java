package com.linyuan.music.mapper;

import com.linyuan.music.model.entity.Artist;
import com.linyuan.music.model.vo.ArtistDetailVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Mapper
public interface ArtistMapper extends BaseMapper<Artist> {

    // 根据id查询歌手详情
    ArtistDetailVO getArtistDetailById(Long artistId);

}
