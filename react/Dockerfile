# Node 20 LTS
FROM node:20

WORKDIR /app

# 複製 package.json 安裝依賴
COPY package*.json ./
RUN npm install

# 複製專案程式碼
COPY . .

# Dev server
CMD ["npm", "run", "dev"]

EXPOSE 3000

