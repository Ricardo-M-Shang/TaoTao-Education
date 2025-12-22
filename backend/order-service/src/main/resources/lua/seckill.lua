local stockKey = KEYS[1]
local userKey = KEYS[2]
local userId = ARGV[1]

-- Check if user already took it
if redis.call('sismember', userKey, userId) == 1 then
    return -1 -- Duplicate
end

-- Check stock
local stock = tonumber(redis.call('get', stockKey))
if stock == nil then
    return -2 -- Not initialized
end
if stock <= 0 then
    return 0 -- Out of stock
end

-- Decr stock
redis.call('decr', stockKey)
-- Record user
redis.call('sadd', userKey, userId)

return 1 -- Success
