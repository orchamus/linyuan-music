#!/usr/bin/env node
/**
 * 林渊音乐 · 曲库 MCP Server（stdio）
 *
 * 把曲库查询能力包装成标准 MCP 工具，供 Spring AI MCP Client（或任意 MCP 客户端）调用：
 *   - search_songs        按关键词搜索歌曲
 *   - songs_by_artist     按歌手查询歌曲
 *   - hot_songs           热门歌曲
 *
 * 数据来源：后端 REST 接口（http://localhost:8080），不直连数据库。
 * 启动方式（由 Spring AI 通过 stdio 拉起）：node index.js
 * 环境变量：MUSIC_API_BASE 可覆盖后端地址，默认 http://localhost:8080
 */
import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";

const API_BASE = process.env.MUSIC_API_BASE || "http://localhost:8080";

const TOOLS = [
  {
    name: "search_songs",
    description: "按关键词（歌名或专辑名）搜索曲库中的歌曲，返回歌名、歌手、专辑、风格",
    inputSchema: {
      type: "object",
      properties: {
        keyword: { type: "string", description: "搜索关键词，例如歌名片段" },
      },
      required: ["keyword"],
    },
  },
  {
    name: "songs_by_artist",
    description: "查询某位歌手的歌曲列表",
    inputSchema: {
      type: "object",
      properties: {
        artistName: { type: "string", description: "歌手名，例如 林渊、周杰伦" },
      },
      required: ["artistName"],
    },
  },
  {
    name: "hot_songs",
    description: "查询平台当前推荐的热门歌曲",
    inputSchema: { type: "object", properties: {} },
  },
];

/** 调用后端分页查询接口 */
async function querySongs(filters) {
  const res = await fetch(`${API_BASE}/song/getAllSongs`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ pageNum: 1, pageSize: 5, ...filters }),
  });
  if (!res.ok) {
    throw new Error(`后端接口返回 ${res.status}`);
  }
  const json = await res.json();
  const records = json?.data?.items ?? [];
  const total = json?.data?.total ?? records.length;
  if (!records.length) {
    return "没有找到符合条件的歌曲。";
  }
  const body = records
    .map(
      (s) =>
        `- ${s.songName} - ${s.artistName}，专辑《${s.album}》` +
        (s.duration ? `，时长 ${s.duration} 秒` : "")
    )
    .join("\n");
  return `共 ${total} 首，以下是前 ${records.length} 首：\n${body}`;
}

/** 调用后端推荐接口 */
async function queryRecommend() {
  const res = await fetch(`${API_BASE}/song/getRecommendedSongs`);
  if (!res.ok) {
    throw new Error(`后端接口返回 ${res.status}`);
  }
  const json = await res.json();
  const list = (json?.data ?? []).slice(0, 5);
  if (!list.length) {
    return "曲库暂时没有推荐歌曲。";
  }
  return list.map((s) => `- ${s.songName} - ${s.artistName}`).join("\n");
}

const server = new Server(
  { name: "linyuan-music-mcp-server", version: "1.0.0" },
  { capabilities: { tools: {} } }
);

server.setRequestHandler(ListToolsRequestSchema, async () => ({ tools: TOOLS }));

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args = {} } = request.params;
  try {
    let text;
    switch (name) {
      case "search_songs":
        text = await querySongs({ songName: args.keyword });
        break;
      case "songs_by_artist":
        text = await querySongs({ artistName: args.artistName });
        break;
      case "hot_songs":
        text = await queryRecommend();
        break;
      default:
        return {
          content: [{ type: "text", text: `未知工具：${name}` }],
          isError: true,
        };
    }
    return { content: [{ type: "text", text }] };
  } catch (err) {
    return {
      content: [{ type: "text", text: `查询失败：${err.message}` }],
      isError: true,
    };
  }
});

const transport = new StdioServerTransport();
await server.connect(transport);
// 注意：stdio 模式下日志不能打到 stdout，否则会破坏 MCP 协议流
process.stderr.write("linyuan-music-mcp-server started (stdio)\n");
