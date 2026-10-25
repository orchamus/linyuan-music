package com.linyuan.music.service.impl;

import com.linyuan.music.model.entity.Genre;
import com.linyuan.music.mapper.GenreMapper;
import com.linyuan.music.service.IGenreService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Service
public class GenreServiceImpl extends ServiceImpl<GenreMapper, Genre> implements IGenreService {

}
