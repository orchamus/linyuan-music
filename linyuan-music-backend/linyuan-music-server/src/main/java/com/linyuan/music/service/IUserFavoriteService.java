package com.linyuan.music.service;

import com.linyuan.music.model.dto.PlaylistDTO;
import com.linyuan.music.model.dto.SongDTO;
import com.linyuan.music.model.entity.UserFavorite;
import com.linyuan.music.model.vo.PlaylistVO;
import com.linyuan.music.model.vo.SongVO;
import com.linyuan.music.result.PageResult;
import com.linyuan.music.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
public interface IUserFavoriteService extends IService<UserFavorite> {

    // 获取用户收藏的歌曲列表
    Result<PageResult<SongVO>> getUserFavoriteSongs(SongDTO songDTO);

    // 收藏歌曲
    Result collectSong(Long songId);

    // 取消收藏歌曲
    Result cancelCollectSong(Long songId);

    // 获取用户收藏的歌单列表
    Result<PageResult<PlaylistVO>> getUserFavoritePlaylists(PlaylistDTO playlistDTO);

    // 收藏歌单
    Result collectPlaylist(Long playlistId);

    // 取消收藏歌单
    Result cancelCollectPlaylist(Long playlistId);

}
