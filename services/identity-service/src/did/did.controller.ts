import {
  Body,
  Controller,
  Get,
  InternalServerErrorException,
  Logger,
  Param,
  Post,
  UseGuards,
} from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBody,
  ApiNotFoundResponse,
  ApiOkResponse,
  ApiOperation,
  ApiParam,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';
const { DIDDocument } = require('did-resolver');
type DIDDocument = typeof DIDDocument;
import { DidService } from './did.service';
import { GenerateDidDTO } from './dtos/GenerateDid.dto';
const pLimit = require('p-limit');
const limit = pLimit(100);

@ApiTags('DID')
@Controller()
export class DidController {
  constructor(private readonly didService: DidService) {}

  @ApiOperation({ summary: 'Generate a new DID' })
  @ApiOkResponse({ description: 'DID Generated', isArray: true })
  @ApiBadRequestResponse({ description: 'Bad request' })
  @ApiBody({ type: GenerateDidDTO, isArray: true })
  @Post('/did/generate')
  async generateDID(
    @Body() generateRequest: { content: GenerateDidDTO[] },
  ): Promise<DIDDocument[]> {
    const promises = generateRequest.content.map((generateDidDTO) => {
      return limit(() => this.didService.generateDID(generateDidDTO));
    });
    try {
      return await Promise.all(promises);
    } catch (err) {
      Logger.error(err);
      throw new InternalServerErrorException(`Error generating one or more DIDs`);
    }
  }

  @ApiOperation({ summary: 'Resolve a DID ID' })
  @ApiOkResponse({ description: 'DID resolved' })
  @ApiBadRequestResponse({ description: 'Bad Request' })
  @ApiNotFoundResponse({ description: 'DID not found' })
  @ApiParam({ name: 'id', description: 'The DID ID to resolve' })
  @Get('/did/resolve/:id')
  async resolveDID(@Param('id') id: string): Promise<DIDDocument> {
    return await this.didService.resolveDID(id);
  }

  @ApiOperation({ summary: 'Resolve a Web DID ID' })
  @ApiOkResponse({ description: 'DID resolved' })
  @ApiBadRequestResponse({ description: 'Bad Request' })
  @ApiNotFoundResponse({ description: 'DID not found' })
  @ApiParam({ name: 'id', description: 'The DID ID to resolve' })
  @Get('/:id/did.json')
  async resolveWebDID(@Param('id') id: string): Promise<DIDDocument> {
    return await this.didService.resolveWebDID(id);
  }
}
