﻿using batch_service.repository;
using Confluent.Kafka;


namespace batch_service.consumer;

public class BatchUpdateService : AbstractConsumerService, IHostedService
{

    private readonly string channel;
    public BatchUpdateService(IConfiguration _configuration, IMongoRepositoryFactory _mongoRepositoryFactory) 
        : base(_configuration, _mongoRepositoryFactory)
    {
        channel = _configuration.GetValue<string>("UpdateChannel");
        
       // RecurringJob.AddOrUpdate(Guid.NewGuid().ToString(), () => OnTimerElapsed(), cronExpression);
    }
    
    public Task StartAsync(CancellationToken cancellationToken)
    {
        var consumerBuilder = new ConsumerBuilder<Ignore, string>(consumerConfig).Build();

        consumerBuilder.Subscribe(channel);

        Task.Run(() => StartConsumerLoop(consumerBuilder, cancellationToken));
        
        return Task.CompletedTask;
    }

    public Task StopAsync(CancellationToken cancellationToken)
    {
        return Task.CompletedTask;
    }

    protected override void DoBatchAction(List<string> batchData)
    {
        var repository = mongoRepositoryFactory.Create();
        
        repository.UpdateManyAsync(batchData).Wait();
        
        Console.WriteLine($"Saved {batchData.Count} new documents");
    }
    

}
